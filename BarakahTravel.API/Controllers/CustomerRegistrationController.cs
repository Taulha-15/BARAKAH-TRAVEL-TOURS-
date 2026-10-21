

using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Data;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using System.Security.Claims;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CustomerRegistrationController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IPasswordHasher<CustomerAccount> _passwordHasher;

    public CustomerRegistrationController(
        AppDbContext context,
        IPasswordHasher<CustomerAccount> passwordHasher)
    {
        _context = context;
        _passwordHasher = passwordHasher;
    }

    // POST: api/CustomerRegistration/register
    [HttpPost("register")]
    public async Task<IActionResult> Register(
        [FromBody] CustomerRegistrationRequest request)
    {
        var code = request.RegistrationCode?.Trim();
        var email = request.Email?.Trim().ToLowerInvariant();

        if (string.IsNullOrWhiteSpace(code) ||
            string.IsNullOrWhiteSpace(email) ||
            string.IsNullOrWhiteSpace(request.FirstName) ||
            string.IsNullOrWhiteSpace(request.Surname) ||
            string.IsNullOrWhiteSpace(request.Phone))
        {
            return BadRequest(new
            {
                success = false,
                message = "Required fields cannot be blank."
            });
        }

        await using var transaction =
            await _context.Database.BeginTransactionAsync(
                IsolationLevel.Serializable);

        var registration = await _context.CustomerRegistrationCodes
            .FirstOrDefaultAsync(c =>
                c.RegistrationCode == code);

        if (registration == null ||
            !registration.IsActive ||
            registration.IsUsed ||
            (registration.ExpiresAt.HasValue &&
             registration.ExpiresAt.Value <= DateTime.UtcNow))
        {
            return BadRequest(new
            {
                success = false,
                message = "Invalid or unavailable registration code."
            });
        }

        var customer = await _context.Customers
            .FirstOrDefaultAsync(c =>
                c.CustomerId == registration.CustomerId);

        if (customer == null)
        {
            return BadRequest(new
            {
                success = false,
                message = "Registration cannot be completed."
            });
        }

        // If staff recorded an email, it must match.
        if (!string.IsNullOrWhiteSpace(customer.Email) &&
            !string.Equals(
                customer.Email.Trim(),
                email,
                StringComparison.OrdinalIgnoreCase))
        {
            return BadRequest(new
            {
                success = false,
                message = "Registration details could not be verified."
            });
        }

        // Prevent duplicate customer accounts.
        var accountExists = await _context.CustomerAccounts
            .AnyAsync(a =>
                a.CustomerId == customer.CustomerId ||
                a.Email == email);

        if (accountExists)
        {
            return BadRequest(new
            {
                success = false,
                message = "An account already exists for these details."
            });
        }

        // Update the existing customer record.
        customer.FirstName = request.FirstName.Trim();
        customer.Surname = request.Surname.Trim();
        customer.Phone = request.Phone.Trim();
        customer.Email = email;
        customer.Idnumber = request.IDNumber;
        customer.PassportNumber = request.PassportNumber;
        customer.DateOfBirth = request.DateOfBirth;
        customer.Gender = request.Gender;
        customer.Address = request.Address;

        // Create the customer login account.
        var account = new CustomerAccount
        {
            CustomerId = customer.CustomerId,
            Email = email,
            IsActive = true,
            CreatedAt = DateTime.UtcNow
        };

        // Never store the plain-text password.
        account.PasswordHash =
            _passwordHasher.HashPassword(
                account,
                request.Password);

        _context.CustomerAccounts.Add(account);

        // Consume the code in the same transaction.
        registration.IsUsed = true;
        registration.IsActive = false;

        await _context.SaveChangesAsync();
        await transaction.CommitAsync();

        return Ok(new
        {
            success = true,
            message = "Customer registration and account creation completed successfully."
        });
    }
}
