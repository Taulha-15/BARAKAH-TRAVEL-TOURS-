
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CustomerAuthController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IPasswordHasher<CustomerAccount> _passwordHasher;

    public CustomerAuthController(
        AppDbContext context,
        IPasswordHasher<CustomerAccount> passwordHasher)
    {
        _context = context;
        _passwordHasher = passwordHasher;
    }

    // POST: api/CustomerAuth/login
    [AllowAnonymous]
    [HttpPost("login")]
    public async Task<IActionResult> Login(
        [FromBody] CustomerLoginRequest request)
    {
        var email = request.Email.Trim().ToLowerInvariant();

        var account = await _context.CustomerAccounts
            .FirstOrDefaultAsync(a =>
                a.Email == email && a.IsActive);

        if (account == null)
        {
            return Unauthorized(new
            {
                success = false,
                message = "Invalid email or password."
            });
        }

        var verification = _passwordHasher.VerifyHashedPassword(
            account,
            account.PasswordHash,
            request.Password);

        if (verification == PasswordVerificationResult.Failed)
        {
            return Unauthorized(new
            {
                success = false,
                message = "Invalid email or password."
            });
        }

        var claims = new List<Claim>
        {
            new Claim(
                ClaimTypes.NameIdentifier,
                account.CustomerId.ToString()),

            new Claim(ClaimTypes.Email, account.Email),

            new Claim(ClaimTypes.Role, "Customer")
        };

        var identity = new ClaimsIdentity(
            claims,
            CookieAuthenticationDefaults.AuthenticationScheme);

        var principal = new ClaimsPrincipal(identity);

        await HttpContext.SignInAsync(
            CookieAuthenticationDefaults.AuthenticationScheme,
            principal);


        var customer = await _context.Customers
            .AsNoTracking()
            .FirstOrDefaultAsync(c =>
                c.CustomerId == account.CustomerId);

        return Ok(new
        {
            success = true,
            message = "Login successful.",
            customerId = account.CustomerId,
            firstName = customer?.FirstName ?? "Customer",
            email = account.Email
        });

    }

    // GET: api/CustomerAuth/me
    [Authorize(Roles = "Customer")]
    [HttpGet("me")]
    public IActionResult Me()
    {
        return Ok(new
        {
            success = true,
            customerId = User.FindFirstValue(
                ClaimTypes.NameIdentifier),
            email = User.FindFirstValue(ClaimTypes.Email)
        });
    }

    // POST: api/CustomerAuth/logout
    [Authorize(Roles = "Customer")]
    [HttpPost("logout")]
    public async Task<IActionResult> Logout()
    {
        await HttpContext.SignOutAsync(
            CookieAuthenticationDefaults.AuthenticationScheme);

        return Ok(new
        {
            success = true,
            message = "Logout successful."
        });
    }
}
