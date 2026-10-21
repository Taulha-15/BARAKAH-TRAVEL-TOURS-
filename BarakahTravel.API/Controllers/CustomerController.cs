
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.Security.Claims;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(
    AuthenticationSchemes = "WebsiteCustomer",
    Roles = "Customer")]
public class CustomersController : ControllerBase
{
    private readonly AppDbContext _context;

    public CustomersController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/Customers/me
    [HttpGet("me")]
    public async Task<IActionResult> GetMyDetails()
    {
        if (!int.TryParse(
            User.FindFirstValue(ClaimTypes.NameIdentifier),
            out int customerId))
        {
            return Unauthorized();
        }

        var customer = await _context.Customers
            .AsNoTracking()
            .Where(c => c.CustomerId == customerId)
            .Select(c => new
            {
                c.CustomerId,
                c.FirstName,
                c.Surname,
                c.Email,
                c.Phone,
                c.Idnumber,
                c.PassportNumber,
                c.DateOfBirth,
                c.Gender,
                c.Address
            })
            .FirstOrDefaultAsync();

        if (customer == null)
        {
            return NotFound();
        }

        return Ok(customer);
    }

    // PUT: api/Customers/me
    [HttpPut("me")]
    public async Task<IActionResult> UpdateMyDetails(
        [FromBody] UpdatePersonalDetailsRequest request)
    {
        if (Request.Headers["X-Barakah-Portal"] != "PersonalDetails")
        {
            return BadRequest("Invalid update request.");
        }

        if (!int.TryParse(
            User.FindFirstValue(ClaimTypes.NameIdentifier),
            out int customerId))
        {
            return Unauthorized();
        }

        var customer = await _context.Customers
            .FirstOrDefaultAsync(c => c.CustomerId == customerId);

        if (customer == null)
        {
            return NotFound();
        }

        customer.FirstName = request.FirstName.Trim();
        customer.Surname = request.Surname.Trim();
        customer.Phone = request.Phone.Trim();

        customer.Idnumber = request.Idnumber?.Trim();
        customer.PassportNumber =
            request.PassportNumber?.Trim();
        customer.DateOfBirth = request.DateOfBirth;
        customer.Gender = request.Gender?.Trim();
        customer.Address = request.Address?.Trim();

        await _context.SaveChangesAsync();

        return Ok(new
        {
            success = true,
            message = "Personal details updated successfully."
        });
    }

    // GET: api/Customers/my-applications
    [HttpGet("my-applications")]
    public async Task<IActionResult> GetMyApplications()
    {
        if (!int.TryParse(
            User.FindFirstValue(ClaimTypes.NameIdentifier),
            out int customerId))
        {
            return Unauthorized();
        }

        var applications = await _context.Applications
            .AsNoTracking()
            .Where(a => a.CustomerId == customerId)
            .OrderByDescending(a => a.CreatedAt)

.Select(a => new
{
    a.ApplicationId,
    a.PackageId,
    PackageName = a.Package.PackageName,
    a.ApplicationStatus,
    a.SubmittedAt,
    a.ReviewedAt,
    a.CreatedAt
})

            .ToListAsync();

        return Ok(applications);
    }

    [HttpPost("my-applications")]
    public async Task<IActionResult> SubmitApplication(
        [FromBody] SubmitApplicationRequest request)
    {
        if (Request.Headers["X-Barakah-Portal"] != "Apply")
        {
            return BadRequest("Invalid application request.");
        }

        if (!int.TryParse(
            User.FindFirstValue(ClaimTypes.NameIdentifier),
            out int customerId))
        {
            return Unauthorized();
        }

        if (request.PackageId <= 0)
        {
            return BadRequest("Please select a valid package.");
        }

        var package = await _context.Packages
            .AsNoTracking()
            .FirstOrDefaultAsync(p =>
                p.PackageId == request.PackageId && p.IsActive);

        if (package == null)
        {
            return BadRequest(
                "The selected Umrah package is unavailable.");
        }

        var existingApplication = await _context.Applications
            .AnyAsync(a =>
                a.CustomerId == customerId &&
                a.PackageId == request.PackageId &&
                (a.ApplicationStatus == "Pending" ||
                 a.ApplicationStatus == "Submitted" ||
                 a.ApplicationStatus == "Under Review"));

        if (existingApplication)
        {
            return Conflict(
                "You already have an active application for this package.");
        }

        var now = DateTime.UtcNow;

        var application = new Application
        {
            CustomerId = customerId,
            PackageId = request.PackageId,
            ApplicationStatus = "Pending",
            SubmittedAt = now,
            CreatedAt = now
        };

        _context.Applications.Add(application);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            success = true,
            applicationId = application.ApplicationId,
            message = "Your Umrah application was submitted successfully."
        });
    }

}

// Information customers are allowed to update
public class UpdatePersonalDetailsRequest
{
    [Required]
    [StringLength(50)]
    public string FirstName { get; set; } = "";

    [Required]
    [StringLength(50)]
    public string Surname { get; set; } = "";

    [Required]
    [StringLength(30)]
    public string Phone { get; set; } = "";

    [StringLength(30)]
    public string? Idnumber { get; set; }

    [StringLength(30)]
    public string? PassportNumber { get; set; }

    public DateOnly? DateOfBirth { get; set; }

    [StringLength(20)]
    public string? Gender { get; set; }

    [StringLength(255)]
    public string? Address { get; set; }
}

public class SubmitApplicationRequest
{
    [Range(1, int.MaxValue)]
    public int PackageId { get; set; }
}

