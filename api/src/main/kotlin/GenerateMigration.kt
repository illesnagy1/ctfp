import com.ctfp.domain.model.*
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

const val MIGRATIONS_DIRECTORY = "src/main/resources/db/migration"

fun main() {
    //val config = HoconApplicationConfig(ConfigFactory.load("application"))

    val pgdb = Database.connect(
        url = "jdbc:postgresql://localhost:5432/ctfp",
        driver = "org.postgresql.Driver",
        user = "ctfp",
        password = "ctfp",
    )
    transaction(pgdb) {
        generateMigrationScript()
    }
}

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun generateMigrationScript() {
    MigrationUtils.generateMigrationScript(
        ChallengeTable,
        scriptDirectory = MIGRATIONS_DIRECTORY,
        scriptName = "V1__Initial_migration",
    )
}
