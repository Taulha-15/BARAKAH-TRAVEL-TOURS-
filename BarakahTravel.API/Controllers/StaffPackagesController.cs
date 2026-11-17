
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffPackagesController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffPackagesController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/StaffPackages
    [HttpGet]
    public async Task<IActionResult> GetPackages()
    {
        var packages = await _context.Packages
            .AsNoTracking()
            .OrderBy(p => p.PackageName)
            .Select(p => new
            {
                id = p.PackageId,
                name = p.PackageName,
                description = p.Description,
                price = p.Price,
                durationDays = p.DurationDays,
                departureDate = p.DepartureDate,
                returnDate = p.ReturnDate,
                isActive = p.IsActive
            })
            .ToListAsync();

        return Ok(packages);
    }

    public class PackageRequest
    {
        public string PackageName { get; set; } = "";
        public string? Description { get; set; }
        public decimal Price { get; set; }
        public int DurationDays { get; set; }
        public DateOnly? DepartureDate { get; set; }
        public DateOnly? ReturnDate { get; set; }
        public bool IsActive { get; set; } = true;
    }

    // POST: api/StaffPackages
    [HttpPost]
    public async Task<IActionResult> CreatePackage(
        [FromBody] PackageRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.PackageName))
            return BadRequest(new { message = "Package name is required." });

        if (request.Price < 0 || request.DurationDays <= 0)
            return BadRequest(new { message = "Enter a valid price and duration." });

        if (request.DepartureDate.HasValue &&
            request.ReturnDate.HasValue &&
            request.ReturnDate.Value < request.DepartureDate.Value)
        {
            return BadRequest(new { message = "Return date cannot be before departure date." });
        }

        var package = new Package
        {
            PackageName = request.PackageName.Trim(),
            Description = request.Description?.Trim(),
            Price = request.Price,
            DurationDays = request.DurationDays,
            DepartureDate = request.DepartureDate,
            ReturnDate = request.ReturnDate,
            IsActive = request.IsActive,
            CreatedAt = DateTime.UtcNow
        };

        _context.Packages.Add(package);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Package created successfully.",
            id = package.PackageId
        });
    }

    // PUT: api/StaffPackages/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdatePackage(
        int id,
        [FromBody] PackageRequest request)
    {
        var package = await _context.Packages
            .FirstOrDefaultAsync(p => p.PackageId == id);

        if (package == null)
            return NotFound(new { message = "Package not found." });

        if (string.IsNullOrWhiteSpace(request.PackageName))
            return BadRequest(new { message = "Package name is required." });

        if (request.Price < 0 || request.DurationDays <= 0)
            return BadRequest(new { message = "Enter a valid price and duration." });

        if (request.DepartureDate.HasValue &&
            request.ReturnDate.HasValue &&
            request.ReturnDate.Value < request.DepartureDate.Value)
        {
            return BadRequest(new { message = "Return date cannot be before departure date." });
        }

        package.PackageName = request.PackageName.Trim();
        package.Description = request.Description?.Trim();
        package.Price = request.Price;
        package.DurationDays = request.DurationDays;
        package.DepartureDate = request.DepartureDate;
        package.ReturnDate = request.ReturnDate;
        package.IsActive = request.IsActive;

        await _context.SaveChangesAsync();

        return Ok(new { message = "Package updated successfully." });
    }

    // PATCH: api/StaffPackages/5/active
    [HttpPatch("{id:int}/active")]
    public async Task<IActionResult> SetPackageActive(
        int id,
        [FromBody] PackageActiveRequest request)
    {
        var package = await _context.Packages
            .FirstOrDefaultAsync(p => p.PackageId == id);

        if (package == null)
            return NotFound(new { message = "Package not found." });

        package.IsActive = request.IsActive;
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Package availability updated.",
            isActive = package.IsActive
        });
    }

    public class PackageActiveRequest
    {
        public bool IsActive { get; set; }
    }
}
