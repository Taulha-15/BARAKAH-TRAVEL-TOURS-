
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PackagesController : ControllerBase
{
    private readonly AppDbContext _context;

    public PackagesController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/Packages
    [HttpGet]
    public async Task<IActionResult> GetPackages()
    {
        var packages = await _context.Packages
            .AsNoTracking()
            .Where(p => p.IsActive)
            .Select(p => new
            {
                p.PackageId,
                p.PackageName,
                p.Description,
                p.Price,
                p.DurationDays,
                p.DepartureDate,
                p.ReturnDate
            })
            .ToListAsync();

        return Ok(packages);
    }
}

