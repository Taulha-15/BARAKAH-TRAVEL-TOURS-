
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(
    AuthenticationSchemes = "WebsiteCustomer",
    Roles = "Customer")]
public class DocumentsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IWebHostEnvironment _environment;

    private const long MaxFileSize = 5 * 1024 * 1024;

    private static readonly string[] AllowedExtensions =
    {
        ".pdf", ".jpg", ".jpeg", ".png"
    };

    private static readonly string[] AllowedDocumentTypes =
    {
        "Passport Copy",
        "ID Document",
        "Other"
    };

    public DocumentsController(
        AppDbContext context,
        IWebHostEnvironment environment)
    {
        _context = context;
        _environment = environment;
    }

    // POST: api/Documents/upload
    [HttpPost("upload")]
    [Consumes("multipart/form-data")]
    [RequestSizeLimit(6 * 1024 * 1024)]
    public async Task<IActionResult> Upload(
        [FromForm] UploadDocumentRequest request)
    {

        if (Request.Headers["X-Barakah-Portal"] != "MyDocuments")
        {
            return BadRequest(new
            {
                success = false,
                message = "Invalid document upload request."
            });
        }

        // Validate selected file.
        if (request.File == null ||
            request.File.Length == 0)
        {
            return BadRequest(new
            {
                success = false,
                message = "Please select a document."
            });
        }

        // Validate file size.
        if (request.File.Length > MaxFileSize)
        {
            return BadRequest(new
            {
                success = false,
                message = "File size must not exceed 5 MB."
            });
        }

        // Validate document type.
        if (string.IsNullOrWhiteSpace(request.DocumentType) ||
            !AllowedDocumentTypes.Contains(request.DocumentType))
        {
            return BadRequest(new
            {
                success = false,
                message = "Please select a valid document type."
            });
        }

        // Validate file extension.
        var extension = Path.GetExtension(
            request.File.FileName).ToLowerInvariant();

        if (!AllowedExtensions.Contains(extension))
        {
            return BadRequest(new
            {
                success = false,
                message =
                    "Only PDF, JPG, JPEG and PNG files are allowed."
            });
        }

        // Validate actual file signature.
        if (!await HasValidFileSignatureAsync(
            request.File, extension))
        {
            return BadRequest(new
            {
                success = false,
                message =
                    "The selected file content does not match its format."
            });
        }

        // Identify authenticated customer.
        if (!int.TryParse(
            User.FindFirstValue(ClaimTypes.NameIdentifier),
            out int customerId))
        {
            return Unauthorized();
        }

        // Confirm application belongs to customer.
        var applicationExists = await _context.Applications
            .AnyAsync(a =>
                a.ApplicationId == request.ApplicationId &&
                a.CustomerId == customerId);

        if (!applicationExists)
        {
            return NotFound(new
            {
                success = false,
                message =
                    "Application not found for this customer."
            });
        }

        // Save files outside wwwroot.
        var storageFolder = Path.Combine(
            _environment.ContentRootPath,
            "PrivateUploads");

        Directory.CreateDirectory(storageFolder);

        // Generate a unique stored filename.
        var storedFileName =
            Guid.NewGuid().ToString("N") + extension;

        var storedPath = Path.Combine(
            storageFolder,
            storedFileName);

        // Save file to private storage.
        await using (var stream = new FileStream(
            storedPath,
            FileMode.CreateNew))
        {
            await request.File.CopyToAsync(stream);
        }

        try
        {
            // Save document information to Azure SQL.
            var document = new Document
            {
                ApplicationId = request.ApplicationId,
                DocumentType = request.DocumentType,
                FileName = Path.GetFileName(
                    request.File.FileName),
                FileUrl = storedFileName,
                UploadedAt = DateTime.UtcNow,
                VerificationStatus = "Pending"
            };

            _context.Documents.Add(document);

            await _context.SaveChangesAsync();

            return Ok(new
            {
                success = true,
                message = "Document uploaded successfully.",
                documentId = document.DocumentId,
                verificationStatus =
                    document.VerificationStatus
            });
        }
        catch
        {
            // Delete saved file if database insert fails.
            if (System.IO.File.Exists(storedPath))
            {
                System.IO.File.Delete(storedPath);
            }

            throw;
        }
    }

    // Verify file content matches the extension.
    private static async Task<bool> HasValidFileSignatureAsync(
        IFormFile file,
        string extension)
    {
        var header = new byte[8];

        await using var stream = file.OpenReadStream();

        var bytesRead = await stream.ReadAsync(
            header.AsMemory(0, header.Length));

        // PDF signature: %PDF-
        if (extension == ".pdf")
        {
            return bytesRead >= 5 &&
                   header[0] == 0x25 &&
                   header[1] == 0x50 &&
                   header[2] == 0x44 &&
                   header[3] == 0x46 &&
                   header[4] == 0x2D;
        }

        // JPEG signature.
        if (extension == ".jpg" ||
            extension == ".jpeg")
        {
            return bytesRead >= 3 &&
                   header[0] == 0xFF &&
                   header[1] == 0xD8 &&
                   header[2] == 0xFF;
        }

        // PNG signature.
        if (extension == ".png")
        {
            byte[] pngSignature =
            {
                0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
            };

            return bytesRead >= 8 &&
                   header.SequenceEqual(pngSignature);
        }

        return false;
    }
}
