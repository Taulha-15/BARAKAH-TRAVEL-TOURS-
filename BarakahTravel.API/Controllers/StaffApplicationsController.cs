
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffApplicationsController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffApplicationsController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/StaffApplications
    [HttpGet]
    public async Task<IActionResult> GetApplications()
    {
        var applications = await _context.Applications
            .AsNoTracking()
            .OrderByDescending(a => a.ApplicationId)
            .Select(a => new
            {
                applicationId = a.ApplicationId,
                customerId = a.CustomerId,
                customerName = a.Customer.FirstName + " " + a.Customer.Surname,
                packageId = a.PackageId,
                packageName = a.Package.PackageName,
                status = a.ApplicationStatus,
                submittedAt = a.SubmittedAt,
                reviewedAt = a.ReviewedAt,
                notes = a.Notes
            })
            .ToListAsync();

        return Ok(applications);
    }


    public class UpdateApplicationStatusRequest
    {
        public string Status { get; set; } = "";
    }

    [HttpPut("{id}/status")]
    public async Task<IActionResult> UpdateApplicationStatus(
        int id,
        [FromBody] UpdateApplicationStatusRequest request)
    {
        var allowedStatuses = new[]
        {
        "Pending",
        "Under Review",
        "Approved"
    };

        if (request == null ||
            !allowedStatuses.Contains(request.Status))
        {
            return BadRequest(new
            {
                message = "Invalid application status."
            });
        }

        var application = await _context.Applications
            .FirstOrDefaultAsync(a => a.ApplicationId == id);

        if (application == null)
        {
            return NotFound(new
            {
                message = "Application not found."
            });
        }

        application.ApplicationStatus = request.Status;
        application.ReviewedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Application status updated successfully.",
            applicationId = application.ApplicationId,
            status = application.ApplicationStatus
        });
    }

}

