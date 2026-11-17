
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffBookingsController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffBookingsController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/StaffBookings
    [HttpGet]
    public async Task<IActionResult> GetBookings()
    {
        var bookings = await _context.Bookings
            .AsNoTracking()
            .OrderByDescending(b => b.BookingDate)
            .Select(b => new
            {
                id = b.BookingId,
                applicationId = b.ApplicationId,
                customerId = b.CustomerId,
                customer = b.Customer.FirstName + " " + b.Customer.Surname,
                packageId = b.PackageId,
                packageName = b.Package.PackageName,
                bookingReference = b.BookingReference,
                status = b.BookingStatus,
                bookingDate = b.BookingDate,
                totalAmount = b.TotalAmount,
                notes = b.Notes
            })
            .ToListAsync();

        return Ok(bookings);
    }

    public class CreateBookingRequest
    {
        public int ApplicationId { get; set; }
        public string? Notes { get; set; }
    }

    // POST: api/StaffBookings
    [HttpPost]
    public async Task<IActionResult> CreateBooking(
        [FromBody] CreateBookingRequest request)
    {
        if (request.ApplicationId <= 0)
            return BadRequest(new { message = "A valid application ID is required." });

        var application = await _context.Applications
            .Include(a => a.Package)
            .FirstOrDefaultAsync(a => a.ApplicationId == request.ApplicationId);

        if (application == null)
            return NotFound(new { message = "Application not found." });

        if (!string.Equals(
            application.ApplicationStatus,
            "Approved",
            StringComparison.OrdinalIgnoreCase))
        {
            return BadRequest(new
            {
                message = "Only approved applications can be booked."
            });
        }

        var existingBooking = await _context.Bookings
            .AnyAsync(b => b.ApplicationId == request.ApplicationId);

        if (existingBooking)
            return Conflict(new { message = "A booking already exists for this application." });

        if (!application.Package.IsActive)
            return BadRequest(new { message = "The selected package is inactive." });

        var booking = new Booking
        {
            ApplicationId = application.ApplicationId,
            CustomerId = application.CustomerId,
            PackageId = application.PackageId,
            BookingReference = "BRK-" +
                Guid.NewGuid().ToString("N")[..10].ToUpperInvariant(),
            BookingStatus = "Pending",
            BookingDate = DateOnly.FromDateTime(DateTime.UtcNow),
            TotalAmount = application.Package.Price,
            Notes = string.IsNullOrWhiteSpace(request.Notes)
                ? null
                : request.Notes.Trim()
        };

        _context.Bookings.Add(booking);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Booking created successfully.",
            id = booking.BookingId,
            applicationId = booking.ApplicationId,
            customerId = booking.CustomerId,
            packageId = booking.PackageId,
            bookingReference = booking.BookingReference,
            status = booking.BookingStatus,
            bookingDate = booking.BookingDate,
            totalAmount = booking.TotalAmount
        });
    }

    public class UpdateBookingStatusRequest
    {
        public string Status { get; set; } = "";
    }

    // PUT: api/StaffBookings/5/status
    [HttpPut("{id:int}/status")]
    public async Task<IActionResult> UpdateBookingStatus(
        int id,
        [FromBody] UpdateBookingStatusRequest request)
    {
        var allowedStatuses = new[]
        {
            "Pending", "Confirmed", "Cancelled", "Completed"
        };

        var status = allowedStatuses.FirstOrDefault(s =>
            s.Equals(request.Status, StringComparison.OrdinalIgnoreCase));

        if (status == null)
            return BadRequest(new { message = "Invalid booking status." });

        var booking = await _context.Bookings
            .FirstOrDefaultAsync(b => b.BookingId == id);

        if (booking == null)
            return NotFound(new { message = "Booking not found." });

        booking.BookingStatus = status;
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Booking status updated successfully.",
            id = booking.BookingId,
            bookingReference = booking.BookingReference,
            status = booking.BookingStatus
        });
    }
}
