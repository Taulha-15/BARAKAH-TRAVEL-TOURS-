
using BarakahTravel.API.Data;
using FirebaseAdmin;
using FirebaseAdmin.Auth;
using Google.Apis.Auth.OAuth2;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class StaffAuthController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffAuthController(AppDbContext context)
    {
        _context = context;
    }

    public class FirebaseLoginRequest
    {
        public string IdToken { get; set; } = string.Empty;
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login(
        [FromBody] FirebaseLoginRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.IdToken))
        {
            return BadRequest(new
            {
                message = "Firebase ID token is required."
            });
        }

        try
        {
            // Initialize Firebase using local credentials.
            if (FirebaseApp.DefaultInstance == null)
            {
                FirebaseApp.Create(new AppOptions
                {
                    ProjectId = "barakahtravel-staffapp",
                    Credential =
                        GoogleCredential.GetApplicationDefault()
                });
            }

            // Verify the Firebase ID token.
            FirebaseToken decodedToken =
                await FirebaseAuth.DefaultInstance
                    .VerifyIdTokenAsync(
                        request.IdToken,
                        checkRevoked: true);

            // Find the staff member linked to this Firebase UID.
            var staff = await _context.Users
                .AsNoTracking()
                .Where(u =>
                    u.FirebaseUid == decodedToken.Uid &&
                    u.IsActive &&
                    (u.Role == "Admin" || u.Role == "Staff"))
                .Select(u => new
                {
                    u.UserId,
                    u.FirstName,
                    u.Surname,
                    u.Role
                })
                .FirstOrDefaultAsync();

            if (staff == null)
            {
                return StatusCode(403, new
                {
                    message = "This Google account is not authorized for staff access."
                });
            }

            return Ok(new
            {
                message = "Staff authentication successful.",
                staff.UserId,
                staff.FirstName,
                staff.Surname,
                staff.Role
            });
        }
        catch (FirebaseAuthException)
        {
            return Unauthorized(new
            {
                message = "Invalid or expired Firebase ID token."
            });
        }
        catch (Exception)
        {
            return StatusCode(503, new
            {
                message = "Staff authentication is temporarily unavailable."
            });
        }
    }
}
