
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffDocumentsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IWebHostEnvironment _environment;

    public StaffDocumentsController(
        AppDbContext context,
        IWebHostEnvironment environment)
    {
        _context = context;
        _environment = environment;
    }

    // GET: api/StaffDocuments
    [HttpGet]
    public async Task<IActionResult> GetDocuments()
    {
        var documents = await _context.Documents
            .AsNoTracking()
            .OrderByDescending(d => d.DocumentId)
            .Select(d => new
            {
                documentId = d.DocumentId,
                applicationId = d.ApplicationId,
                customerName =
                    d.Application.Customer.FirstName + " " +
                    d.Application.Customer.Surname,
                documentType = d.DocumentType,
                fileName = d.FileName,
                fileUrl = d.FileUrl,
                verificationStatus = d.VerificationStatus,
                uploadedAt = d.UploadedAt,
                verifiedAt = d.VerifiedAt
            })
            .ToListAsync();

        return Ok(documents);
    }

    public class UpdateDocumentStatusRequest
    {
        public string Status { get; set; } = "";
    }

    // PUT: api/StaffDocuments/{id}/status
    [HttpPut("{id}/status")]
    public async Task<IActionResult> UpdateDocumentStatus(
        int id,
        [FromBody] UpdateDocumentStatusRequest request)
    {
        if (request == null ||
            (request.Status != "Accepted" &&
             request.Status != "Rejected" &&
             request.Status != "Pending"))
        {
            return BadRequest(new
            {
                message = "Invalid document verification status."
            });
        }

        var document = await _context.Documents
            .FirstOrDefaultAsync(d => d.DocumentId == id);

        if (document == null)
        {
            return NotFound(new
            {
                message = "Document not found."
            });
        }

        document.VerificationStatus = request.Status;

        document.VerifiedAt = request.Status == "Pending"
            ? null
            : DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Document verification status updated successfully.",
            documentId = document.DocumentId,
            verificationStatus = document.VerificationStatus,
            verifiedAt = document.VerifiedAt
        });
    }

    // GET: api/StaffDocuments/{id}/file
    [HttpGet("{id}/file")]
    public async Task<IActionResult> GetDocumentFile(int id)
    {
        var document = await _context.Documents
            .AsNoTracking()
            .FirstOrDefaultAsync(d => d.DocumentId == id);

        if (document == null)
        {
            return NotFound(new
            {
                message = "Document not found."
            });
        }

        if (string.IsNullOrWhiteSpace(document.FileUrl))
        {
            return NotFound(new
            {
                message = "No uploaded file is available."
            });
        }

        // Only allow private stored filenames.
        // Do not expose legacy test paths.
        var storedFileName = document.FileUrl;

        if (storedFileName != Path.GetFileName(storedFileName) ||
            storedFileName.Contains('/') ||
            storedFileName.Contains('\\'))
        {
            return NotFound(new
            {
                message =
                    "This document does not have a private uploaded file."
            });
        }

        var extension = Path.GetExtension(storedFileName)
            .ToLowerInvariant();

        var contentType = extension switch
        {
            ".pdf" => "application/pdf",
            ".jpg" or ".jpeg" => "image/jpeg",
            ".png" => "image/png",
            _ => null
        };

        if (contentType == null)
        {
            return BadRequest(new
            {
                message = "Unsupported document format."
            });
        }

        var filePath = Path.Combine(
            _environment.ContentRootPath,
            "PrivateUploads",
            storedFileName
        );

        if (!System.IO.File.Exists(filePath))
        {
            return NotFound(new
            {
                message = "Uploaded file was not found on the server."
            });
        }

        var stream = new FileStream(
            filePath,
            FileMode.Open,
            FileAccess.Read,
            FileShare.Read
        );

        return File(stream, contentType);
    }
}
