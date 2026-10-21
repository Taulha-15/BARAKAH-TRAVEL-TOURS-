using System;
using System.Collections.Generic;
using BarakahTravel.API.Models;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Data;

public partial class AppDbContext : DbContext

{
    public AppDbContext()
    {
    }

    public AppDbContext(DbContextOptions<AppDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Application> Applications { get; set; }

    public virtual DbSet<Booking> Bookings { get; set; }

    public virtual DbSet<Customer> Customers { get; set; }

    public virtual DbSet<CustomerAccount> CustomerAccounts { get; set; }

    public virtual DbSet<CustomerRegistrationCode> CustomerRegistrationCodes { get; set; }

    public virtual DbSet<Document> Documents { get; set; }

    public virtual DbSet<Package> Packages { get; set; }

    public virtual DbSet<Payment> Payments { get; set; }

    public virtual DbSet<TravelDetail> TravelDetails { get; set; }

    public virtual DbSet<TravelSchedule> TravelSchedules { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        => optionsBuilder.UseSqlServer("Name=ConnectionStrings:DefaultConnection");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Application>(entity =>
        {
            entity.HasKey(e => e.ApplicationId).HasName("PK__Applicat__C93A4F79A9F8D54A");

            entity.Property(e => e.ApplicationStatus).HasDefaultValue("Draft");
            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(getutcdate())");

            entity.HasOne(d => d.Customer).WithMany(p => p.Applications)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Applications_Customers");

            entity.HasOne(d => d.Package).WithMany(p => p.Applications)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Applications_Packages");

            entity.HasOne(d => d.ReviewedByNavigation).WithMany(p => p.Applications).HasConstraintName("FK_Applications_Reviewer");
        });

        modelBuilder.Entity<Booking>(entity =>
        {
            entity.HasKey(e => e.BookingId).HasName("PK__Bookings__73951ACDBA75DC6B");

            entity.Property(e => e.BookingDate).HasDefaultValueSql("(CONVERT([date],getutcdate()))");
            entity.Property(e => e.BookingStatus).HasDefaultValue("Pending");

            entity.HasOne(d => d.Application).WithMany(p => p.Bookings)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Bookings_Applications");

            entity.HasOne(d => d.Customer).WithMany(p => p.Bookings)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Bookings_Customers");

            entity.HasOne(d => d.Package).WithMany(p => p.Bookings)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Bookings_Packages");
        });

        modelBuilder.Entity<Customer>(entity =>
        {
            entity.HasKey(e => e.CustomerId).HasName("PK__Customer__A4AE64B87E036DB6");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(getutcdate())");
        });

        modelBuilder.Entity<CustomerAccount>(entity =>
        {
            entity.HasKey(e => e.CustomerAccountId).HasName("PK__Customer__4212CC6A470760F7");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.IsActive).HasDefaultValue(true);

            entity.HasOne(d => d.Customer).WithOne(p => p.CustomerAccount)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_CustomerAccounts_Customers");
        });

        modelBuilder.Entity<CustomerRegistrationCode>(entity =>
        {
            entity.HasKey(e => e.RegistrationCodeId).HasName("PK__Customer__16460FEE258FE6FC");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(getutcdate())");
            entity.Property(e => e.IsActive).HasDefaultValue(true);

            entity.HasOne(d => d.Customer).WithMany(p => p.CustomerRegistrationCodes)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_RegistrationCodes_Customers");

        });

        modelBuilder.Entity<Document>(entity =>
        {
            entity.HasKey(e => e.DocumentId).HasName("PK__Document__1ABEEF6F35628DA3");

            entity.Property(e => e.UploadedAt).HasDefaultValueSql("(getutcdate())");
            entity.Property(e => e.VerificationStatus).HasDefaultValue("Pending");

            entity.HasOne(d => d.Application).WithMany(p => p.Documents)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Documents_Applications");

            entity.HasOne(d => d.VerifiedByNavigation).WithMany(p => p.Documents).HasConstraintName("FK_Documents_VerifiedBy");
        });

        modelBuilder.Entity<Package>(entity =>
        {
            entity.HasKey(e => e.PackageId).HasName("PK__Packages__322035EC87DE2CB2");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(getutcdate())");
            entity.Property(e => e.IsActive).HasDefaultValue(true);
        });

        modelBuilder.Entity<Payment>(entity =>
        {
            entity.HasKey(e => e.PaymentId).HasName("PK__Payments__9B556A5856D77B61");

            entity.Property(e => e.PaymentStatus).HasDefaultValue("Recorded");

            entity.HasOne(d => d.Booking).WithMany(p => p.Payments)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Payments_Bookings");

            entity.HasOne(d => d.Customer).WithMany(p => p.Payments)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Payments_Customers");

            entity.HasOne(d => d.RecordedByNavigation).WithMany(p => p.Payments)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Payments_Users");
        });

        modelBuilder.Entity<TravelDetail>(entity =>
        {
            entity.HasKey(e => e.TravelDetailId).HasName("PK__TravelDe__0D5B845641DF630B");

            entity.HasOne(d => d.Application).WithMany(p => p.TravelDetails)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_TravelDetails_Applications");
        });

        modelBuilder.Entity<TravelSchedule>(entity =>
        {
            entity.HasKey(e => e.ScheduleId).HasName("PK__TravelSc__9C8A5B69733CEE17");

            entity.Property(e => e.ScheduleStatus).HasDefaultValue("Scheduled");

            entity.HasOne(d => d.Booking).WithMany(p => p.TravelSchedules)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_TravelSchedules_Bookings");
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.UserId).HasName("PK__Users__1788CCAC54E570A0");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(getutcdate())");
            entity.Property(e => e.IsActive).HasDefaultValue(true);
            entity.Property(e => e.Role).HasDefaultValue("Staff");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
