package net.dzikoysk.funnyguilds.guild.placeholders;

import net.dzikoysk.funnyguilds.FunnyGuildsSpec
import net.dzikoysk.funnyguilds.guild.Guild
import net.dzikoysk.funnyguilds.guild.permission.GuildPermissionChecker
import net.dzikoysk.funnyguilds.user.FakeUserProfile
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.lenient
import java.util.*

class MemberPlaceholdersTest : FunnyGuildsSpec() {

    @Mock
    private lateinit var guildPermissionChecker: GuildPermissionChecker

    private lateinit var guildPlaceholdersService: GuildPlaceholdersService

    @BeforeEach
    fun setup() {
        lenient().`when`(funnyGuilds.guildPermissionChecker).thenReturn(guildPermissionChecker)
        lenient().`when`(funnyGuilds.name).thenReturn("FunnyGuilds")
        
        guildPlaceholdersService = GuildPlaceholdersService()
        guildPlaceholdersService.register(funnyGuilds, "simple", GuildPlaceholdersService.createSimplePlaceholders(funnyGuilds))
        guildPlaceholdersService.register(funnyGuilds, "members", GuildPlaceholdersService.createMemberPlaceholders(funnyGuilds))
    }

    @Test
    fun `test G-MEMBER-X placeholder with online and offline members`() {
        val guild = guildManager.addGuild(Guild("TestGuild", "TEST"))
        
        // Create users with different online statuses
        val owner = userManager.createFake(UUID.randomUUID(), "Owner", FakeUserProfile.online())
        val deputy = userManager.createFake(UUID.randomUUID(), "Deputy", FakeUserProfile.online())
        val member1 = userManager.createFake(UUID.randomUUID(), "Member1", FakeUserProfile.offline())
        val member2 = userManager.createFake(UUID.randomUUID(), "Member2", FakeUserProfile.online())
        
        guild.setOwner(owner)
        guild.addDeputy(deputy)
        guild.addMember(member1)
        guild.addMember(member2)
        
        // Format member-1 placeholder (should be owner, online)
        var text = "{G-MEMBER-1}"
        text = guildPlaceholdersService.format(null, text, guild)
        
        // Should contain green color for online and owner's name
        assertTrue(text.contains("Owner"))
        assertTrue(text.contains("&a") || text.contains("§a"))
        
        // Format member-2 placeholder (should be deputy, online) 
        text = "{G-MEMBER-2}"
        text = guildPlaceholdersService.format(null, text, guild)
        
        // Should contain green color for online and deputy's name
        assertTrue(text.contains("Deputy"))
        assertTrue(text.contains("&a") || text.contains("§a"))
        
        // Format member-3 placeholder (should be Member2, online)
        text = "{G-MEMBER-3}"
        text = guildPlaceholdersService.format(null, text, guild)
        
        // Should contain green color for online and member's name
        assertTrue(text.contains("Member2"))
        assertTrue(text.contains("&a") || text.contains("§a"))
        
        // Format member-4 placeholder (should be Member1, offline)
        text = "{G-MEMBER-4}"
        text = guildPlaceholdersService.format(null, text, guild)
        
        // Should contain gray color for offline and member's name
        assertTrue(text.contains("Member1"))
        assertTrue(text.contains("&7") || text.contains("§7"))
    }

    @Test
    fun `test G-MEMBER-X placeholder with no value when index exceeds member count`() {
        val guild = guildManager.addGuild(Guild("TestGuild", "TEST"))
        
        val owner = userManager.createFake(UUID.randomUUID(), "Owner", FakeUserProfile.online())
        guild.setOwner(owner)
        
        // Format member-5 placeholder (guild has only 1 member)
        var text = "{G-MEMBER-5}"
        text = guildPlaceholdersService.format(null, text, guild)
        
        // Should contain the no-value message
        assertTrue(text.contains("Brak"))
    }

    @Test
    fun `test G-MEMBER-X placeholder respects vanish`() {
        val guild = guildManager.addGuild(Guild("TestGuild", "TEST"))
        
        // Create a vanished user
        val vanishedUser = userManager.createFake(UUID.randomUUID(), "VanishedUser", FakeUserProfile.vanished())
        guild.setOwner(vanishedUser)
        
        config.gMemberRespectVanish = true
        
        // Format member-1 placeholder
        var text = "{G-MEMBER-1}"
        text = guildPlaceholdersService.format(null, text, guild)
        
        // Should contain gray color for "offline" (vanished) user
        assertTrue(text.contains("VanishedUser"))
        assertTrue(text.contains("&7") || text.contains("§7"))
    }

    @Test
    fun `test G-MEMBER-X placeholder sorting by name`() {
        val guild = guildManager.addGuild(Guild("TestGuild", "TEST"))
        
        // Create members with names that should be sorted alphabetically (all same priority, all offline)
        val zUser = userManager.createFake(UUID.randomUUID(), "Zorro", FakeUserProfile.offline())
        val aUser = userManager.createFake(UUID.randomUUID(), "Alpha", FakeUserProfile.offline())
        val mUser = userManager.createFake(UUID.randomUUID(), "Mike", FakeUserProfile.offline())
        
        guild.setOwner(zUser)
        guild.addMember(aUser)
        guild.addMember(mUser)
        
        // Format member-1 placeholder - should be "Alpha" (alphabetically first among owner)
        // Note: Owner has priority 1, so Zorro should be first as owner
        var text = "{G-MEMBER-1}"
        text = guildPlaceholdersService.format(null, text, guild)
        assertTrue(text.contains("Zorro")) // Owner comes first
        
        // Format member-2 placeholder - should be "Alpha" (alphabetically first among regular members)
        text = "{G-MEMBER-2}"
        text = guildPlaceholdersService.format(null, text, guild)
        assertTrue(text.contains("Alpha"))
        
        // Format member-3 placeholder - should be "Mike"
        text = "{G-MEMBER-3}"
        text = guildPlaceholdersService.format(null, text, guild)
        assertTrue(text.contains("Mike"))
    }
}
