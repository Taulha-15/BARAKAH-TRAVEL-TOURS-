
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffTravelSchedulesController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffTravelSchedulesController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/StaffTravelSchedules
    [HttpGet]
    public async Task<IActionResult> GetSchedules()
    {
        var schedules = await _context.TravelSchedules
            .AsNoTracking()
            .OrderBy(s => s.DepartureDate)
            .Select(s => new
            {
                id = s.ScheduleId,
                bookingId = s.BookingId,
                bookingReference = s.Booking.BookingReference,
                customer = s.Booking.Customer.FirstName + " " +
                           s.Booking.Customer.Surname,
                packageName = s.Booking.Package.PackageName,
                departureDate = s.DepartureDate,
                returnDate = s.ReturnDate,
                departureTime = s.DepartureTime,
                returnTime = s.ReturnTime,
                departureAirport = s.DepartureAirport,
                arrivalAirport = s.ArrivalAirport,
                airline = s.Airline,
                flightNumber = s.FlightNumber,
                status = s.ScheduleStatus,
                notes = s.Notes
            })
            .ToListAsync();

        return Ok(schedules);
    }

    public class TravelScheduleRequest
    {
        public int BookingId { get; set; }
        public DateOnly DepartureDate { get; set; }
        public DateOnly ReturnDate { get; set; }
        public TimeOnly? DepartureTime { get; set; }
        public TimeOnly? ReturnTime { get; set; }
        public string? DepartureAirport { get; set; }
        public string? ArrivalAirport { get; set; }
        public string? Airline { get; set; }
        public string? FlightNumber { get; set; }
        public string? Status { get; set; }
        public string? Notes { get; set; }
    }

    // POST: api/StaffTravelSchedules
    [HttpPost]
    public async Task<IActionResult> CreateSchedule(
        [FromBody] TravelScheduleRequest request)
    {
        if (request.BookingId <= 0)
            return BadRequest(new { message = "A valid booking ID is required." });

        if (request.DepartureDate == default ||
            request.ReturnDate == default ||
            request.ReturnDate < request.DepartureDate)
        {
            return BadRequest(new { message = "Enter valid departure and return dates." });
        }

        var bookingExists = await _context.Bookings
            .AnyAsync(b => b.BookingId == request.BookingId);

        if (!bookingExists)
            return NotFound(new { message = "Booking not found." });

        var allowedStatuses = new[]
        {
            "Scheduled", "Confirmed", "Completed", "Cancelled"
        };

        var requestedStatus = string.IsNullOrWhiteSpace(request.Status)
            ? "Scheduled"
            : allowedStatuses.FirstOrDefault(s =>
                s.Equals(request.Status, StringComparison.OrdinalIgnoreCase));

        if (requestedStatus == null)
            return BadRequest(new { message = "Invalid travel schedule status." });

        var schedule = new TravelSchedule
        {
            BookingId = request.BookingId,
            DepartureDate = request.DepartureDate,
            ReturnDate = request.ReturnDate,
            DepartureTime = request.DepartureTime,
            ReturnTime = request.ReturnTime,
            DepartureAirport = request.DepartureAirport?.Trim(),
            ArrivalAirport = request.ArrivalAirport?.Trim(),
            Airline = request.Airline?.Trim(),
            FlightNumber = request.FlightNumber?.Trim(),
            ScheduleStatus = requestedStatus,
            Notes = request.Notes?.Trim()
        };

        _context.TravelSchedules.Add(schedule);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Travel schedule created successfully.",
            id = schedule.ScheduleId,
            bookingId = schedule.BookingId,
            status = schedule.ScheduleStatus
        });
    }

    // PUT: api/StaffTravelSchedules/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateSchedule(
        int id,
        [FromBody] TravelScheduleRequest request)
    {
        var schedule = await _context.TravelSchedules
            .FirstOrDefaultAsync(s => s.ScheduleId == id);

        if (schedule == null)
            return NotFound(new { message = "Travel schedule not found." });

        if (request.DepartureDate == default ||
            request.ReturnDate == default ||
            request.ReturnDate < request.DepartureDate)
        {
            return BadRequest(new { message = "Enter valid departure and return dates." });
        }

        var allowedStatuses = new[]
        {
            "Scheduled", "Confirmed", "Completed", "Cancelled"
        };

        var requestedStatus = string.IsNullOrWhiteSpace(request.Status)
            ? schedule.ScheduleStatus
            : allowedStatuses.FirstOrDefault(s =>
                s.Equals(request.Status, StringComparison.OrdinalIgnoreCase));

        if (requestedStatus == null)
            return BadRequest(new { message = "Invalid travel schedule status." });

        schedule.DepartureDate = request.DepartureDate;
        schedule.ReturnDate = request.ReturnDate;
        schedule.DepartureTime = request.DepartureTime;
        schedule.ReturnTime = request.ReturnTime;
        schedule.DepartureAirport = request.DepartureAirport?.Trim();
        schedule.ArrivalAirport = request.ArrivalAirport?.Trim();
        schedule.Airline = request.Airline?.Trim();
        schedule.FlightNumber = request.FlightNumber?.Trim();
        schedule.ScheduleStatus = requestedStatus;
        schedule.Notes = request.Notes?.Trim();

        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Travel schedule updated successfully.",
            id = schedule.ScheduleId,
            status = schedule.ScheduleStatus
        });
    }
}
