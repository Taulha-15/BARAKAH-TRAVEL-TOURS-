
using BarakahTravel.API.Data;
using FirebaseAdmin;
using FirebaseAdmin.Auth;
using Google.Apis.Auth.OAuth2;
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

    public class GoogleLoginRequest
    {
        public string IdToken { get; set; } = string.Empty;
    }

    // POST: api/CustomerAuth/google
    // Single sign-on for customers: the website signs in with Google through Firebase,
    // then sends the Firebase ID token here. The API verifies the token with Firebase Admin
    // and links it to an existing customer account by (verified) email address.
    [AllowAnonymous]
    [HttpPost("google")]
    public async Task<IActionResult> GoogleLogin(
        [FromBody] GoogleLoginRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.IdToken))
        {
            return BadRequest(new
            {
                success = false,
                message = "Google ID token is required."
            });
        }

        try
        {
            // Same Firebase project and credentials the staff app uses.
            if (FirebaseApp.DefaultInstance == null)
            {
                FirebaseApp.Create(new AppOptions
                {
                    ProjectId = "barakahtravel-staffapp",
                    Credential = GoogleCredential.GetApplicationDefault()
                });
            }

            // Checks the signature, expiry, audience and issuer of the token.
            FirebaseToken decoded =
                await FirebaseAuth.DefaultInstance
                    .VerifyIdTokenAsync(request.IdToken, checkRevoked: true);

            // Only trust the email address if Google has verified it.
            var emailVerified =
                decoded.Claims.TryGetValue("email_verified", out var verified) &&
                verified is bool isVerified && isVerified;

            if (!emailVerified ||
                !decoded.Claims.TryGetValue("email", out var emailClaim) ||
                emailClaim is not string rawEmail)
            {
                return Unauthorized(new
                {
                    success = false,
                    message = "Your Google email address is not verified."
                });
            }

            var email = rawEmail.Trim().ToLowerInvariant();

            // Customers are registered by staff first; Google only signs in an existing account.
            var account = await _context.CustomerAccounts
                .FirstOrDefaultAsync(a =>
                    a.Email == email && a.IsActive);

            if (account == null)
            {
                return StatusCode(403, new
                {
                    success = false,
                    message = "No customer account uses this Google email."
                });
            }

            var customer = await _context.Customers
                .AsNoTracking()
                .FirstOrDefaultAsync(c =>
                    c.CustomerId == account.CustomerId);

            return Ok(new
            {
                success = true,
                message = "Google sign-in successful.",
                customerId = account.CustomerId,
                firstName = customer?.FirstName ?? "Customer",
                email = account.Email
            });
        }
        catch (FirebaseAuthException)
        {
            return Unauthorized(new
            {
                success = false,
                message = "Invalid or expired Google sign-in."
            });
        }
        catch (Exception)
        {
            return StatusCode(503, new
            {
                success = false,
                message = "Google sign-in is temporarily unavailable."
            });
        }
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
