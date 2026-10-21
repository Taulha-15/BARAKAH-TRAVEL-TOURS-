
using BarakahTravel.API.Data;
using BarakahTravel.API.Models;

using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using BarakahTravel.API.Security;
using Microsoft.AspNetCore.Authorization;

// Create the application
var builder = WebApplication.CreateBuilder(args);

// Firebase project ID
const string firebaseProjectId = "barakahtravel-staffapp";

// Shared authentication encryption keys
// Local development only
builder.Services
    .AddDataProtection()
    .SetApplicationName("BarakahTravel.SharedAuth")
    .PersistKeysToFileSystem(
        new DirectoryInfo(@"C:\BarakahTravelDevKeys"));

// Azure SQL database connection
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString(
            "DefaultConnection")));

// Password hashing for customer accounts
builder.Services.AddScoped<
    IPasswordHasher<CustomerAccount>,
    PasswordHasher<CustomerAccount>>();

// Authentication
builder.Services
    .AddAuthentication(
        CookieAuthenticationDefaults.AuthenticationScheme)

    // Existing API authentication cookie
    .AddCookie(
        CookieAuthenticationDefaults.AuthenticationScheme,
        options =>
        {
            options.Cookie.Name =
                "BarakahTravel.API.Customer";

            options.Cookie.HttpOnly = true;

            options.Cookie.SecurePolicy =
                CookieSecurePolicy.Always;

            options.Cookie.SameSite =
                SameSiteMode.Lax;

            options.ExpireTimeSpan =
                TimeSpan.FromHours(2);

            options.SlidingExpiration = true;

            options.Events.OnRedirectToLogin = context =>
            {
                context.Response.StatusCode = 401;
                return Task.CompletedTask;
            };

            options.Events.OnRedirectToAccessDenied = context =>
            {
                context.Response.StatusCode = 403;
                return Task.CompletedTask;
            };
        })

    // Existing authentication scheme for website customers
    .AddCookie("WebsiteCustomer", options =>
    {
        options.Cookie.Name =
            "BarakahTravel.Web.Customer";

        options.Cookie.HttpOnly = true;

        options.Cookie.SecurePolicy =
            CookieSecurePolicy.Always;

        options.Cookie.SameSite =
            SameSiteMode.Lax;

        options.ExpireTimeSpan =
            TimeSpan.FromHours(2);

        options.SlidingExpiration = true;

        options.Events.OnRedirectToLogin = context =>
        {
            context.Response.StatusCode = 401;
            return Task.CompletedTask;
        };

        options.Events.OnRedirectToAccessDenied = context =>
        {
            context.Response.StatusCode = 403;
            return Task.CompletedTask;
        };
    })

    // Firebase JWT authentication for the Android staff app
    .AddJwtBearer("FirebaseStaff", options =>
    {
        options.Authority =
            $"https://securetoken.google.com/{firebaseProjectId}";

        options.Audience = firebaseProjectId;

        options.RequireHttpsMetadata = true;

        options.MapInboundClaims = false;

        options.TokenValidationParameters =
            new TokenValidationParameters
            {
                ValidateIssuer = true,
                ValidIssuer =
                    $"https://securetoken.google.com/{firebaseProjectId}",

                ValidateAudience = true,
                ValidAudience = firebaseProjectId,

                ValidateLifetime = true,
                RequireExpirationTime = true,

                ValidateIssuerSigningKey = true,

                NameClaimType = "user_id"
            };
    });

// Match the website's authentication ticket format.
// The website issues tickets using the "Cookies" scheme.
builder.Services
    .AddOptions<CookieAuthenticationOptions>("WebsiteCustomer")
    .Configure<IDataProtectionProvider>(
        (options, dataProtection) =>
        {
            var protector = dataProtection.CreateProtector(
                "Microsoft.AspNetCore.Authentication.Cookies.CookieAuthenticationMiddleware",
                CookieAuthenticationDefaults.AuthenticationScheme,
                "v2");

            options.TicketDataFormat =
                new TicketDataFormat(protector);
        });

// Database-backed staff authorization
builder.Services.AddScoped<
    IAuthorizationHandler,
    StaffAuthorizationHandler>();

builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("StaffOnly", policy =>
    {
        policy.AuthenticationSchemes.Add("FirebaseStaff");
        policy.RequireAuthenticatedUser();
        policy.AddRequirements(new StaffRequirement());
    });
});

// API controllers and Swagger
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Build the application
var app = builder.Build();

// Swagger for development
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Middleware
app.UseHttpsRedirection();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();
