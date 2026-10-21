
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RegistrationCodesController : ControllerBase
{
    private readonly AppDbContext _context;

    public RegistrationCodesController(AppDbContext context)
    {
        _context = context;
    }

    // POST: api/RegistrationCodes/validate
    [HttpPost("validate")]
    public async Task<IActionResult> ValidateCode(
        [FromBody] ValidateRegistrationCodeRequest request)
    {
        var code = request.RegistrationCode.Trim();

        if (string.IsNullOrWhiteSpace(code))
        {
            return BadRequest(new
            {
                isValid = false,
                message = "Please enter a registration code."
            });
        }

        var now = DateTime.UtcNow;

        var isValid = await _context.CustomerRegistrationCodes
            .AsNoTracking()
            .AnyAsync(c =>
                c.RegistrationCode == code &&
                c.IsActive &&
                !c.IsUsed &&
                (c.ExpiresAt == null || c.ExpiresAt > now));

        return Ok(new
        {
            isValid,
            message = isValid
                ? "Registration code is valid."
                : "Invalid, expired, inactive, or already-used registration code."
        });
    }
}

public class ValidateRegistrationCodeRequest
{
    [Required]
    [StringLength(50)]
    public string RegistrationCode { get; set; } = string.Empty;
}
