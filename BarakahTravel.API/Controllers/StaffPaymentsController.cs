
using System.Security.Claims;
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Policy = "StaffOnly")]
public class StaffPaymentsController : ControllerBase
{
    private readonly AppDbContext _context;

    public StaffPaymentsController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/StaffPayments
    [HttpGet]
    public async Task<IActionResult> GetPayments()
    {
        var payments = await _context.Payments
            .AsNoTracking()
            .OrderByDescending(p => p.PaymentDate)
            .Select(p => new
            {
                id = p.PaymentId,
                bookingId = p.BookingId,
                customerId = p.CustomerId,
                customer = p.Customer.FirstName + " " + p.Customer.Surname,
                bookingReference = p.Booking.BookingReference,
                amount = p.Amount,
                paymentDate = p.PaymentDate,
                paymentMethod = p.PaymentMethod,
                referenceNumber = p.ReferenceNumber,
                status = p.PaymentStatus,
                notes = p.Notes,
                recordedBy = p.RecordedByNavigation.FirstName + " " +
                             p.RecordedByNavigation.Surname
            })
            .ToListAsync();

        return Ok(payments);
    }

    public class CreatePaymentRequest
    {
        public int BookingId { get; set; }
        public decimal Amount { get; set; }
        public DateOnly PaymentDate { get; set; }
        public string? PaymentMethod { get; set; }
        public string? ReferenceNumber { get; set; }
        public string? Notes { get; set; }
    }

    // POST: api/StaffPayments
    [HttpPost]
    public async Task<IActionResult> CreatePayment(
        [FromBody] CreatePaymentRequest request)
    {
        if (request.BookingId <= 0)
            return BadRequest(new { message = "A valid booking ID is required." });

        if (request.Amount <= 0)
            return BadRequest(new { message = "Payment amount must be greater than zero." });

        var booking = await _context.Bookings
            .FirstOrDefaultAsync(b => b.BookingId == request.BookingId);

        if (booking == null)
            return NotFound(new { message = "Booking not found." });

        var firebaseUid =
            User.FindFirst("user_id")?.Value ??
            User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

        if (string.IsNullOrWhiteSpace(firebaseUid))
            return Unauthorized(new { message = "Staff identity could not be verified." });

        var staffUser = await _context.Users
            .FirstOrDefaultAsync(u =>
                u.FirebaseUid == firebaseUid &&
                u.IsActive &&
                (u.Role == "Admin" || u.Role == "Staff"));

        if (staffUser == null)
            return Unauthorized(new { message = "Staff account not found." });

        var payment = new Payment
        {
            BookingId = booking.BookingId,
            CustomerId = booking.CustomerId,
            Amount = request.Amount,
            PaymentDate = request.PaymentDate == default
                ? DateOnly.FromDateTime(DateTime.UtcNow)
                : request.PaymentDate,
            PaymentMethod = string.IsNullOrWhiteSpace(request.PaymentMethod)
                ? null
                : request.PaymentMethod.Trim(),
            ReferenceNumber = string.IsNullOrWhiteSpace(request.ReferenceNumber)
                ? null
                : request.ReferenceNumber.Trim(),
            PaymentStatus = "Recorded",
            RecordedBy = staffUser.UserId,
            Notes = string.IsNullOrWhiteSpace(request.Notes)
                ? null
                : request.Notes.Trim()
        };

        _context.Payments.Add(payment);
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Payment recorded successfully.",
            id = payment.PaymentId,
            bookingId = payment.BookingId,
            customerId = payment.CustomerId,
            amount = payment.Amount,
            paymentDate = payment.PaymentDate,
            paymentMethod = payment.PaymentMethod,
            referenceNumber = payment.ReferenceNumber,
            status = payment.PaymentStatus
        });
    }

    public class UpdatePaymentStatusRequest
    {
        public string Status { get; set; } = "";
    }

    // PUT: api/StaffPayments/5/status
    [HttpPut("{id:int}/status")]
    public async Task<IActionResult> UpdatePaymentStatus(
        int id,
        [FromBody] UpdatePaymentStatusRequest request)
    {
        var allowedStatuses = new[]
        {
            "Recorded", "Pending", "Verified", "Rejected"
        };

        var status = allowedStatuses.FirstOrDefault(s =>
            s.Equals(request.Status, StringComparison.OrdinalIgnoreCase));

        if (status == null)
            return BadRequest(new { message = "Invalid payment status." });

        var payment = await _context.Payments
            .FirstOrDefaultAsync(p => p.PaymentId == id);

        if (payment == null)
            return NotFound(new { message = "Payment not found." });

        payment.PaymentStatus = status;
        await _context.SaveChangesAsync();

        return Ok(new
        {
            message = "Payment status updated successfully.",
            id = payment.PaymentId,
            status = payment.PaymentStatus
        });
    }
}
