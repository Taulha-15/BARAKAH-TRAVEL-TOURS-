
using BarakahTravel.API.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace BarakahTravel.API.Security;

public class StaffRequirement : IAuthorizationRequirement
{
}

public class StaffAuthorizationHandler
    : AuthorizationHandler<StaffRequirement>
{
    private readonly AppDbContext _context;

    public StaffAuthorizationHandler(AppDbContext context)
    {
        _context = context;
    }

    protected override async Task HandleRequirementAsync(
        AuthorizationHandlerContext context,
        StaffRequirement requirement)
    {
        if (context.User.Identity?.IsAuthenticated != true)
            return;

        // Firebase tokens contain the UID in the user_id claim.
        var firebaseUid =
            context.User.FindFirst("user_id")?.Value
            ?? context.User.FindFirst(ClaimTypes.NameIdentifier)?.Value;

        if (string.IsNullOrWhiteSpace(firebaseUid))
            return;

        // Check authorization against Azure SQL on each request.
        var isAuthorized = await _context.Users
            .AsNoTracking()
            .AnyAsync(user =>
                user.FirebaseUid == firebaseUid &&
                user.IsActive &&
                (user.Role == "Admin" || user.Role == "Staff"));

        if (isAuthorized)
        {
            context.Succeed(requirement);
        }
    }
}
