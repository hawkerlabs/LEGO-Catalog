

package com.elifox.legocatalog.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import com.elifox.legocatalog.garden.data.GardenPlanting
import com.elifox.legocatalog.garden.data.GardenPlantingDao
import com.elifox.legocatalog.util.getValue
import com.elifox.legocatalog.util.testCalendar
import com.elifox.legocatalog.util.testGardenPlanting
import com.elifox.legocatalog.util.testPlant
import com.elifox.legocatalog.util.testPlants
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GardenPlantingDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var gardenPlantingDao: GardenPlantingDao
    private var testGardenPlantingId: Long = 0

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        gardenPlantingDao = database.gardenPlantingDao()

        database.legoSetDao().insertAll(testPlants)
        testGardenPlantingId = gardenPlantingDao.insertGardenPlanting(testGardenPlanting)
    }

    @After fun closeDb() {
        database.close()
    }

    @Test fun testGetGardenPlantings() {
        val gardenPlanting2 = GardenPlanting(
                testPlants[1].plantId,
                testCalendar,
                testCalendar
        ).also { it.gardenPlantingId = 2 }
        gardenPlantingDao.insertGardenPlanting(gardenPlanting2)
        assertThat(getValue(gardenPlantingDao.getGardenPlantings()).size, equalTo(2))
    }

    @Test
    fun testGetGardenPlanting() {
        assertThat(
            getValue(gardenPlantingDao.getGardenPlanting(testGardenPlantingId)),
            equalTo(testGardenPlanting)
        )
    }

    @Test fun testDeleteGardenPlanting() {
        val gardenPlanting2 = GardenPlanting(
                testPlants[1].plantId,
                testCalendar,
                testCalendar
        ).also { it.gardenPlantingId = 2 }
        gardenPlantingDao.insertGardenPlanting(gardenPlanting2)
        assertThat(getValue(gardenPlantingDao.getGardenPlantings()).size, equalTo(2))
        gardenPlantingDao.deleteGardenPlanting(gardenPlanting2)
        assertThat(getValue(gardenPlantingDao.getGardenPlantings()).size, equalTo(1))
    }

    @Test fun testGetGardenPlantingForPlant() {
        assertThat(getValue(gardenPlantingDao.getGardenPlantingForPlant(testPlant.plantId)),
                equalTo(testGardenPlanting))
    }

    @Test fun testGetGardenPlantingForPlant_notFound() {
        assertNull(getValue(gardenPlantingDao.getGardenPlantingForPlant(testPlants[2].plantId)))
    }

    @Test fun testGetPlantAndGardenPlantings() {
        val plantAndGardenPlantings = getValue(gardenPlantingDao.getPlantAndGardenPlantings())
        assertThat(plantAndGardenPlantings.size, equalTo(3))

        /**
         * Only the [testPlant] has been planted, and thus has an associated [GardenPlanting]
         */
        assertThat(plantAndGardenPlantings[0].plant, equalTo(testPlant))
        assertThat(plantAndGardenPlantings[0].gardenPlantings.size, equalTo(1))
        assertThat(plantAndGardenPlantings[0].gardenPlantings[0], equalTo(testGardenPlanting))

        // The other legoSets in the database have not been planted and thus have no GardenPlantings
        assertThat(plantAndGardenPlantings[1].gardenPlantings.size, equalTo(0))
        assertThat(plantAndGardenPlantings[2].gardenPlantings.size, equalTo(0))
    }
}