
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BarakahTravel.API.Models;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffCustomersController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffCustomersController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/StaffCustomers
    [HttpGet]
    public async Task<IActionResult> GetCustomers()
    {
        var customers = await _context.Customers
            .AsNoTracking()
            .OrderBy(c => c.CustomerId)
            .Select(c => new
            {
                c.CustomerId,
                c.FirstName,
                c.Surname,
                c.Email,
                c.Phone,
                c.Status
            })
            .ToListAsync();

        return Ok(customers);
    }

    public class UpdateCustomerRequest
    {
        public string FirstName { get; set; } = "";
        public string Surname { get; set; } = "";
        public string Email { get; set; } = "";
        public string Phone { get; set; } = "";
        public string Status { get; set; } = "Pending";
    }

    // PUT: api/StaffCustomers/1
    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateCustomer(
        int id,
        [FromBody] UpdateCustomerRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.FirstName) ||
            string.IsNullOrWhiteSpace(request.Surname) ||
            string.IsNullOrWhiteSpace(request.Email))
        {
            return BadRequest(new
            {
                message = "First name, surname and email are required."
            });
        }

        if (request.Status != "Pending" &&
            request.Status != "Review" &&
            request.Status != "Verified")
        {
            return BadRequest(new
            {
                message = "Invalid customer status."
            });
        }

        var customer = await _context.Customers
            .FirstOrDefaultAsync(c => c.CustomerId == id);

        if (customer == null)
        {
            return NotFound(new
            {
                message = "Customer not found."
            });
        }

        customer.FirstName = request.FirstName.Trim();
        customer.Surname = request.Surname.Trim();
        customer.Email = request.Email.Trim();
        customer.Phone = request.Phone?.Trim() ?? "";
        customer.Status = request.Status;

        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Customer updated successfully.",
            customer.CustomerId,
            customer.FirstName,
            customer.Surname,
            customer.Email,
            customer.Phone,
            customer.Status
        });
    }


    // POST: api/StaffCustomers/1/generate-code
    [HttpPost("{id:int}/generate-code")]
    public async Task<IActionResult> GenerateRegistrationCode(int id)
    {
        await using var transaction =
            await _context.Database.BeginTransactionAsync(
                System.Data.IsolationLevel.Serializable);

        var customerExists = await _context.Customers
            .AnyAsync(c => c.CustomerId == id);

        if (!customerExists)
            return NotFound(new { message = "Customer not found." });

        // Customers with website accounts must not receive new codes.
        var hasAccount = await _context.CustomerAccounts
            .AnyAsync(a => a.CustomerId == id);

        if (hasAccount)
            return Conflict(new
            {
                message = "This customer already has a website account."
            });

        var now = DateTime.UtcNow;

        // Reuse a valid, unused code.
        var existingCode = await _context.CustomerRegistrationCodes
            .Where(c =>
                c.CustomerId == id &&
                c.IsActive &&
                !c.IsUsed &&
                (c.ExpiresAt == null || c.ExpiresAt > now))
            .OrderByDescending(c => c.CreatedAt)
            .FirstOrDefaultAsync();

        if (existingCode != null)
        {
            return Ok(new
            {
                message = "Existing registration code retrieved.",
                registrationCode = existingCode.RegistrationCode,
                expiresAt = existingCode.ExpiresAt
            });
        }

        // Deactivate old codes before issuing a replacement.
        var oldCodes = await _context.CustomerRegistrationCodes
            .Where(c => c.CustomerId == id && c.IsActive)
            .ToListAsync();

        foreach (var oldCode in oldCodes)
            oldCode.IsActive = false;

        // Generate an unpredictable code rather than a sequential ID.
        var newCode = "BTT-" +
            Guid.NewGuid().ToString("N")[..12].ToUpperInvariant();

        var registration = new CustomerRegistrationCode
        {
            CustomerId = id,
            RegistrationCode = newCode,
            CreatedAt = now,
            ExpiresAt = now.AddDays(30),
            IsActive = true,
            IsUsed = false
        };

        _context.CustomerRegistrationCodes.Add(registration);
        await _context.SaveChangesAsync();
        await transaction.CommitAsync();

        return Ok(new
        {
            message = "Registration code generated successfully.",
            registrationCode = newCode,
            expiresAt = registration.ExpiresAt
        });
    }

}
