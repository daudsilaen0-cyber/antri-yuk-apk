package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AntriYukDao {
    @Query("SELECT * FROM salons ORDER BY isOpen DESC, name ASC")
    fun observeSalons(): Flow<List<SalonEntity>>

    @Query("SELECT COUNT(*) FROM salons")
    suspend fun getSalonCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSalons(salons: List<SalonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSalon(salon: SalonEntity)

    @Query("SELECT * FROM salon_services ORDER BY category ASC, name ASC")
    fun observeServices(): Flow<List<SalonServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServices(services: List<SalonServiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertService(service: SalonServiceEntity)

    @Query("DELETE FROM salon_services WHERE id = :serviceId")
    suspend fun deleteServiceById(serviceId: String)

    @Query("SELECT * FROM hairstylists ORDER BY name ASC")
    fun observeStylists(): Flow<List<HairstylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStylists(stylists: List<HairstylistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStylist(stylist: HairstylistEntity)

    @Query("DELETE FROM hairstylists WHERE id = :stylistId")
    suspend fun deleteStylistById(stylistId: String)

    @Query("SELECT * FROM queue_tickets ORDER BY createdAtMillis ASC")
    fun observeTickets(): Flow<List<QueueTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTickets(tickets: List<QueueTicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTicket(ticket: QueueTicketEntity)
}
