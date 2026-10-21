
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TravelDetailsController : ControllerBase
{
    private readonly AppDbContext _context;

    public TravelDetailsController(AppDbContext context)
    {
        _context = context;
    }

    // POST: api/TravelDetails
    [HttpPost]
    public async Task<IActionResult> CreateTravelDetails(
        [FromBody] CreateTravelDetailsRequest request)
    {
        // Confirm that the application exists.
        var applicationExists = await _context.Applications
            .AnyAsync(a =>
                a.ApplicationId == request.ApplicationId);

        if (!applicationExists)
        {
            return BadRequest(new
            {
                success = false,
                message = "Application does not exist."
            });
        }

        // Prevent duplicate travel details for one application.
        var detailsExist = await _context.TravelDetails
            .AnyAsync(t =>
                t.ApplicationId == request.ApplicationId);

        if (detailsExist)
        {
            return Conflict(new
            {
                success = false,
                message = "Travel details already exist for this application."
            });
        }

        var travelDetails = new TravelDetail
        {
            ApplicationId = request.ApplicationId,
            DepartureCity = request.DepartureCity,
            Destination = request.Destination,
            DepartureDate = request.DepartureDate,
            ReturnDate = request.ReturnDate,
            Airline = request.Airline,
            FlightNumber = request.FlightNumber,
            AdditionalNotes = request.AdditionalNotes
        };

        _context.TravelDetails.Add(travelDetails);

        await _context.SaveChangesAsync();

        return Ok(new
        {
            success = true,
            message = "Travel details saved successfully.",
            travelDetailId = travelDetails.TravelDetailId,
            applicationId = travelDetails.ApplicationId
        });
    }
}
