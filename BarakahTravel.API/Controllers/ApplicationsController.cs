
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "Customer")]
public class ApplicationsController : ControllerBase
{
    private readonly AppDbContext _context;

    public ApplicationsController(AppDbContext context)
    {
        _context = context;
    }

    // POST: api/Applications
    [HttpPost]
    public async Task<IActionResult> CreateApplication(
        [FromBody] CreateApplicationRequest request)
    {
        // Identify the customer from their login cookie.
        var customerIdValue = User.FindFirstValue(
            ClaimTypes.NameIdentifier);

        if (!int.TryParse(customerIdValue, out int customerId))
        {
            return Unauthorized(new
            {
                success = false,
                message = "Customer authentication is required."
            });
        }

        // Confirm the customer account is still active.
        var accountIsActive = await _context.CustomerAccounts
            .AnyAsync(a =>
                a.CustomerId == customerId &&
                a.IsActive);

        if (!accountIsActive)
        {
            return Unauthorized(new
            {
                success = false,
                message = "Customer account is unavailable."
            });
        }

        // Check the selected package.
        var packageExists = await _context.Packages
            .AnyAsync(p =>
                p.PackageId == request.PackageId &&
                p.IsActive);

        if (!packageExists)
        {
            return BadRequest(new
            {
                success = false,
                message = "Selected package is unavailable."
            });
        }

        // The CustomerId comes from authentication,
        // never from the submitted request.
        var application = new Application
        {
            CustomerId = customerId,
            PackageId = request.PackageId,
            ApplicationStatus = "Pending",
            SubmittedAt = DateTime.UtcNow,
            CreatedAt = DateTime.UtcNow,
            Notes = request.Notes
        };

        _context.Applications.Add(application);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            success = true,
            message = "Travel application submitted successfully.",
            applicationId = application.ApplicationId,
            status = application.ApplicationStatus
        });
    }
}
