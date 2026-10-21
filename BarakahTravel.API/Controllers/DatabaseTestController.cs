
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Data.SqlClient;

namespace BarakahTravel.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DatabaseTestController : ControllerBase
{
    private readonly AppDbContext _context;

    public DatabaseTestController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<IActionResult> Get()
    {
        try
        {
            var canConnect = await _context.Database.CanConnectAsync();

            if (!canConnect)
            {
                return StatusCode(503, new
                {
                    status = "failed",
                    message = "The API could not connect to the database."
                });
            }

            return Ok(new
            {
                status = "success",
                message = "Connected to Azure SQL successfully."
            });
        }
        catch (SqlException)
        {
            return StatusCode(503, new
            {
                status = "failed",
                message = "Database connection failed. Check credentials, firewall settings and database availability."
            });
        }
    }
}
