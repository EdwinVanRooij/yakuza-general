/* 
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program unader any cother version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.server.PlayerBuffValueHolder;
import net.server.PlayerCoolDownValueHolder;
import net.server.PlayerDiseaseValueHolder;
import net.server.Server;
import net.server.channel.Channel;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleMessenger;
import net.server.world.MapleMessengerCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import scripting.event.EventInstanceManager;
import server.CashShop;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.TimerManager;
import server.events.MapleEvents;
import server.events.RescueGaga;
import server.events.gm.MapleFitness;
import server.events.gm.MapleOla;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.HiredMerchant;
import server.maps.MapleDoor;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapEffect;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.maps.SavedLocation;
import server.maps.SavedLocationType;
import server.partyquest.MonsterCarnival;
import server.partyquest.MonsterCarnivalParty;
import server.partyquest.PartyQuest;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import client.autoban.AutobanManager;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleWeaponType;
import client.inventory.ModifyInventory;
import constants.ExpTable;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Bowmaster;
import constants.skills.Buccaneer;
import constants.skills.Corsair;
import constants.skills.Crusader;
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.GM;
import constants.skills.Hermit;
import constants.skills.Hero;
import constants.skills.ILArchMage;
import constants.skills.Magician;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.Paladin;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Shadower;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.Swordsman;
import constants.skills.ThunderBreaker;

public class MapleCharacter extends AbstractAnimatedMapleMapObject {

    private static final String LEVEL_200 = "[Congrats] %s has reached Level 200! Congratulate %s on such an amazing achievement!";
    private static final int[] DEFAULT_KEY = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41, 39};
    private static final int[] DEFAULT_TYPE = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4, 4};
    private static final int[] DEFAULT_ACTION = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23, 27};
    private static final String[] BLOCKED_NAMES = {"admin", "owner", "moderator", "intern", "donor", "administrator", "help", "helper", "alert", "notice", "maplestory", "Solaxia", "fuck", "wizet", "fucking", "negro", "fuk", "fuc", "penis", "pussy", "asshole", "gay",
        "nigger", "homo", "suck", "cum", "shit", "shitty", "condom", "security", "official", "rape", "nigga", "sex", "tit", "boner", "orgy", "clit", "asshole", "fatass", "bitch", "support", "gamemaster", "cock", "gaay", "gm",
        "operate", "master", "sysop", "party", "GameMaster", "community", "message", "event", "test", "meso", "Scania", "renewal", "yata", "AsiaSoft", "henesys"};
    private int world;
    private int accountid, id;
    private int rank, rankMove, jobRank, jobRankMove;
    private int level, str, dex, luk, int_, hp, maxhp, mp, maxmp;
    private int hpMpApUsed;
    private int hair;
    private int face;
    private int remainingAp;
    private int[] remainingSp = new int[10];
    private int fame;
    private int initialSpawnPoint;
    private int mapid;
    private int gender;
    private int currentPage, currentType = 0, currentTab = 1;
    private int chair;
    private int itemEffect;
    private int guildid, guildrank, allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private MapleFamily family;
    private int familyId;
    private int bookCover;
    private int markedMonster = 0;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int dojoPoints, vanquisherStage, dojoStage, dojoEnergy, vanquisherKills;
    private int warpToId;
    private int expRate = 1, mesoRate = 1, dropRate = 1;
    private int omokwins, omokties, omoklosses, matchcardwins, matchcardties, matchcardlosses;
    private int married;
    private long dojoFinish, lastfametime, lastUsedCashItem, lastHealed, lastMesoDrop = -1;
    private transient int localmaxhp, localmaxmp, localstr, localdex, localluk, localint_, magic, watk;
    private boolean hidden, canDoor = true, Berserk, hasMerchant, whiteChat = false;
    private int linkedLevel = 0;
    private String linkedName = null;
    private boolean finishedDojoTutorial, dojoParty;
    private String name;
    private String chalktext;
    private String dataString;
    private String search = null;
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger gachaexp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private int merchantmeso;
    private BuddyList buddylist;
    private EventInstanceManager eventInstance = null;
    private HiredMerchant hiredMerchant = null;
    private MapleClient client;
    private MapleGuildCharacter mgc = null;
    private MaplePartyCharacter mpc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.BEGINNER;
    private MapleMap map, dojoMap;//Make a Dojo pq instance
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3];
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private SavedLocation savedLocations[];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private Map<Short, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<>();
    private Map<Integer, String> entered = new LinkedHashMap<>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<>();
    private Map<Skill, SkillEntry> skills = new LinkedHashMap<>();
    private EnumMap<MapleBuffStat, MapleBuffStatValueHolder> effects = new EnumMap<>(MapleBuffStat.class);
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<>(50);
    private EnumMap<MapleDisease, DiseaseValueHolder> diseases = new EnumMap<>(MapleDisease.class);
    private List<MapleDoor> doors = new ArrayList<>();
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private ScheduledFuture<?>[] fullnessSchedule = new ScheduledFuture<?>[3];
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule, beholderBuffSchedule, BerserkSchedule;
    private ScheduledFuture<?> expiretask;
    private ScheduledFuture<?> recoveryTask;
    private List<ScheduledFuture<?>> timers = new ArrayList<>();
    private NumberFormat nf = new DecimalFormat("#,###,###,###");
    private ArrayList<Integer> excluded = new ArrayList<>();
    private MonsterBook monsterbook;
    private List<MapleRing> crushRings = new ArrayList<>();
    private List<MapleRing> friendshipRings = new ArrayList<>();
    private MapleRing marriageRing;
    private static String[] ariantroomleader = new String[3];
    private static int[] ariantroomslot = new int[3];
    private CashShop cashshop;
    private long portaldelay = 0, lastcombo = 0;
    private short combocounter = 0;
    private List<String> blockedPortals = new ArrayList<>();
    private Map<Short, String> area_info = new LinkedHashMap<>();
    private AutobanManager autoban;
    private boolean isbanned = false;
    private ScheduledFuture<?> pendantOfSpirit = null; //1122017
    private byte pendantExp = 0, lastmobcount = 0;
    private List<Integer> trockmaps = new ArrayList<>();
    private List<Integer> viptrockmaps = new ArrayList<>();
    private Map<String, MapleEvents> events = new LinkedHashMap<>();
    private PartyQuest partyQuest = null;
    private boolean loggedIn = false;
    private MapleDragon dragon = null;

    private MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        savedLocations = new SavedLocation[SavedLocationType.values().length];
        for (int i = 0; i < remainingSp.length; i++) {
            remainingSp[i] = 0;
        }
        for (MapleInventoryType type : MapleInventoryType.values()) {
            byte b = 24;
            if (type == MapleInventoryType.CASH) {
                b = 96;
            }
            inventory[type.ordinal()] = new MapleInventory(type, b);
        }
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = null;
        }
        quests = new LinkedHashMap<>();
        setPosition(new Point(0, 0));
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = 0;
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.maplemount = null;
        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(24);
        for (int i = 0; i < DEFAULT_KEY.length; i++) {
            ret.keymap.put(DEFAULT_KEY[i], new MapleKeyBinding(DEFAULT_TYPE[i], DEFAULT_ACTION[i]));
        }
        //to fix the map 0 lol
        for (int i = 0; i < 5; i++) {
            ret.trockmaps.add(999999999);
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps.add(999999999);
        }

        return ret;
    }

    public int gmLevel() {
        return gmLevel;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(skillId)) {
            this.coolDowns.remove(skillId);
        }
        this.coolDowns.put(skillId, new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public MapleRing getRingById(int id) {
        for (MapleRing ring : getCrushRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        for (MapleRing ring : getFriendshipRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        if (getMarriageRing().getRingId() == id) {
            return getMarriageRing();
        }

        return null;
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (!dojoParty) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addExcluded(int x) {
        excluded.add(x);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        updateSingleStat(MapleStat.HP, getHp());
        updateSingleStat(MapleStat.MP, getMp());
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str += up;
            updateSingleStat(MapleStat.STR, str);
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, dex);
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, int_);
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, luk);
        }
        recalcLocalStats();
    }

    public int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob jobtype = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (jobtype.isA(MapleJob.BEGINNER)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.WARRIOR) || jobtype.isA(MapleJob.DAWNWARRIOR1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001)) > 0) {
                MaxHP += 20;
            } else {
                MaxHP += 8;
            }
        } else if (jobtype.isA(MapleJob.MAGICIAN) || jobtype.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (jobtype.isA(MapleJob.BOWMAN) || jobtype.isA(MapleJob.WINDARCHER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.THIEF) || jobtype.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.PIRATE) || jobtype.isA(MapleJob.THUNDERBREAKER1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000)) > 0) {
                MaxHP += 18;
            } else {
                MaxHP += 8;
            }
        }
        return MaxHP;
    }

    public int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (player.getJob().isA(MapleJob.BEGINNER) || player.getJob().isA(MapleJob.NOBLESSE) || player.getJob().isA(MapleJob.LEGEND)) {
            MaxMP += 6;
        } else if (player.getJob().isA(MapleJob.WARRIOR) || player.getJob().isA(MapleJob.DAWNWARRIOR1) || player.getJob().isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (player.getJob().isA(MapleJob.MAGICIAN) || player.getJob().isA(MapleJob.BLAZEWIZARD1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001)) > 0) {
                MaxMP += 18;
            } else {
                MaxMP += 14;
            }

        } else if (player.getJob().isA(MapleJob.BOWMAN) || player.getJob().isA(MapleJob.THIEF)) {
            MaxMP += 10;
        } else if (player.getJob().isA(MapleJob.PIRATE)) {
            MaxMP += 14;
        }

        return MaxMP;
    }

    public void addSummon(int id, MapleSummon summon) {
        summons.put(id, summon);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason) {
        this.isbanned = true;
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
                ps.setString(1, reason);
                ps.setInt(2, accountid);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }

    }

    public static boolean ban(String id, String reason, boolean accountId) {
        PreparedStatement ps = null;
		ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }

            boolean ret = false;
            ps.setString(1, id);
            rs = ps.executeQuery();
			if (rs.next()) {
				try (PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
					psb.setString(1, reason);
					psb.setInt(2, rs.getInt(1));
					psb.executeUpdate();
				}
				ret = true;
            }
			rs.close();
            ps.close();
            return ret;
        } catch (SQLException ignored) {
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
				if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
		Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
		if (weapon_item != null) {
			MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
			int mainstat;
			int secondarystat;
			if (getJob().isA(MapleJob.THIEF) && weapon == MapleWeaponType.DAGGER_OTHER) {
				weapon = MapleWeaponType.DAGGER_THIEVES;
			}

			if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW || weapon == MapleWeaponType.GUN) {
				mainstat = localdex;
				secondarystat = localstr;
			} else if (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER_THIEVES) {
				mainstat = localluk;
				secondarystat = localdex + localstr;
			} else {
				mainstat = localstr;
				secondarystat = localdex;
			}
			maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
		} else {
            if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
				double weapMulti = 3;
                if (job.getId() % 100 != 0) {
					weapMulti = 4.2;
                }

				int attack = (int) Math.min(Math.floor((2 * getLevel() + 31) / 3), 31);

				maxbasedamage = (int) (localstr * weapMulti + localdex) * attack / 100;
			} else {
				maxbasedamage = 1;
			}
		}
        return maxbasedamage;
    }

    public void cancelAllBuffs(boolean disconnect) {
        if (disconnect) {
            effects.clear();
        } else {
            for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Collections.singletonList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void setCombo(short count) {
        if (count < combocounter) {
            cancelEffectFromBuffStat(MapleBuffStat.ARAN_COMBO);
        }
        combocounter = (short) Math.min(30000, count);
        if (count > 0) {
            announce(MaplePacketCreator.showCombo(combocounter));
        }
    }

    public void setLastCombo(long time) {
        lastcombo = time;
    }

    public short getCombo() {
        return combocounter;
    }

    public long getLastCombo() {
        return lastcombo;
    }

    public int getLastMobCount() { //Used for skills that have mobCount at 1. (a/b)
        return lastmobcount;
    }

    public void setLastMobCount(byte count) {
        lastmobcount = count;
    }

    public void newClient(MapleClient c) {
        this.loggedIn = true;
        c.setAccountName(this.client.getAccountName());//No null's for accountName
        this.client = c;
        MaplePortal portal = map.findClosestSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
        this.map = c.getChannelServer().getMapFactory().getMap(getMapId());
    }

    public void cancelBuffEffects() {
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            mbsvh.schedule.cancel(false);
        }
        this.effects.clear();
    }

    public String getMedalText() {
        String medal = "";
        final Item medalItem = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + MapleItemInformationProvider.getInstance().getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.client.announce(MaplePacketCreator.skillCooldown(skillId, 0));
            }
        }
    }

    public void cancelEffect(int itemId) {
        cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1);
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<>(statups.size());
            buffstats.addAll(statups.stream().map(Pair<MapleBuffStat, Integer>::getLeft).collect(Collectors.toList()));
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                if (party != null) {
                    for (MaplePartyCharacter partyMembers : getParty().getMembers()) {
                        partyMembers.getPlayer().getDoors().remove(door);
                        partyMembers.getDoors().remove(door);
                    }
                    silentPartyUpdate();
                } else {
                    clearDoors();
                }
            }
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GM.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxmp)));
            statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
            client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                this.getMount().cancelSchedule();
                this.getMount().setActive(false);
            }
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        MapleBuffStatValueHolder effect = effects.get(stat);
        if (effect != null) {
            cancelEffect(effect.effect, false, -1);
        }
    }

    public void Hide(boolean hide, boolean login) {
        if (isGM() && hide != this.hidden) {
            if (!hide) {
                this.hidden = false;
                announce(MaplePacketCreator.getGMEffect(0x10, (byte) 0));
                List<MapleBuffStat> dsstat = Collections.singletonList(MapleBuffStat.DARKSIGHT);
                getMap().broadcastGMMessage(this, MaplePacketCreator.cancelForeignBuff(id, dsstat), false);
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                updatePartyMemberHP();
            } else {
                this.hidden = true;
                announce(MaplePacketCreator.getGMEffect(0x10, (byte) 1));
                if (!login) {
                    getMap().broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                }
                getMap().broadcastGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
                getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(id, dsstat), false);
                for (MapleMonster mon : this.getControlledMonsters()) {
                    mon.setController(null);
                    mon.setControllerHasAggro(false);
                    mon.setControllerKnowsAboutAggro(false);
                    mon.getMap().updateMonsterController(mon);
                }
            }
            announce(MaplePacketCreator.enableActions());
        }
    }

    public void Hide(boolean hide) {
        Hide(hide, false);
    }

    public void toggleHide(boolean login) {
        Hide(!isHidden());
    }

    private void cancelFullnessSchedule(int petSlot) {
        if (fullnessSchedule[petSlot] != null) {
            fullnessSchedule[petSlot].cancel(false);
        }
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public static boolean canCreateChar(String name) {
        for (String nameTest : BLOCKED_NAMES) {
            if (name.toLowerCase().contains(nameTest)) {
                return false;
            }
        }
        return getIdByName(name) < 0 && Pattern.compile("[a-zA-Z0-9]{3,12}").matcher(name).matches();
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel > 0) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(from.getId())) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void setMasteries(int jobId) {
        int[] skills = new int[4];
        for (int i = 0; i > skills.length; i++) {
        	skills[i] = 0; //that initalization meng
        }
        if (jobId == 112) {
            skills[0] = Hero.ACHILLES;
            skills[1] = Hero.MONSTER_MAGNET;
            skills[2] = Hero.BRANDISH;
        } else if (jobId == 122) {
            skills[0] = Paladin.ACHILLES;
            skills[1] = Paladin.MONSTER_MAGNET;
            skills[2] = Paladin.BLAST;
        } else if (jobId == 132) {
            skills[0] = DarkKnight.BEHOLDER;
            skills[1] = DarkKnight.ACHILLES;
            skills[2] = DarkKnight.MONSTER_MAGNET;
        } else if (jobId == 212) {
            skills[0] = FPArchMage.BIG_BANG;
            skills[1] = FPArchMage.MANA_REFLECTION;
            skills[2] = FPArchMage.PARALYZE;
        } else if (jobId == 222) {
            skills[0] = ILArchMage.BIG_BANG;
            skills[1] = ILArchMage.MANA_REFLECTION;
            skills[2] = ILArchMage.CHAIN_LIGHTNING;
        } else if (jobId == 232) {
            skills[0] = Bishop.BIG_BANG;
            skills[1] = Bishop.MANA_REFLECTION;
            skills[2] = Bishop.HOLY_SHIELD;
        } else if (jobId == 312) {
            skills[0] = Bowmaster.BOW_EXPERT;
            skills[1] = Bowmaster.HAMSTRING;
            skills[2] = Bowmaster.SHARP_EYES;
        } else if (jobId == 322) {
            skills[0] = Marksman.MARKSMAN_BOOST;
            skills[1] = Marksman.BLIND;
            skills[2] = Marksman.SHARP_EYES;
        } else if (jobId == 412) {
            skills[0] = NightLord.SHADOW_STARS;
            skills[1] = NightLord.SHADOW_SHIFTER;
            skills[2] = NightLord.VENOMOUS_STAR;
        } else if (jobId == 422) {
            skills[0] = Shadower.SHADOW_SHIFTER;
            skills[1] = Shadower.VENOMOUS_STAB;
            skills[2] = Shadower.BOOMERANG_STEP;
        } else if (jobId == 512) {
            skills[0] = Buccaneer.BARRAGE;
            skills[1] = Buccaneer.ENERGY_ORB;
            skills[2] = Buccaneer.SPEED_INFUSION;
            skills[3] = Buccaneer.DRAGON_STRIKE;
        } else if (jobId == 522) {
            skills[0] = Corsair.ELEMENTAL_BOOST;
            skills[1] = Corsair.BULLSEYE;
            skills[2] = Corsair.WRATH_OF_THE_OCTOPI;
            skills[3] = Corsair.RAPID_FIRE;
        } else if (jobId == 2112) {
            skills[0] = Aran.OVER_SWING;
            skills[1] = Aran.HIGH_MASTERY;
            skills[2] = Aran.FREEZE_STANDING;
        } else if (jobId == 2217) {
        	skills[0] = Evan.MAPLE_WARRIOR;
        	skills[1] = Evan.ILLUSION;
        } else if (jobId == 2218) {
        	skills[0] = Evan.BLESSING_OF_THE_ONYX;
        	skills[1] = Evan.BLAZE;
        }
        for (Integer skillId : skills) {
            if (skillId != 0) {
                Skill skill = SkillFactory.getSkill(skillId);
                changeSkillLevel(skill, (byte) 0, 10, -1);
            }
        }
    }

    public void changeJob(MapleJob newJob) {
        if (newJob == null) {
            return;//the fuck you doing idiot!
        }
        this.job = newJob;
        remainingSp[GameConstants.getSkillBook(newJob.getId())] += 1;
        if (GameConstants.hasSPTable(newJob)) {
            remainingSp[GameConstants.getSkillBook(newJob.getId())] += 2;
        } else {
            if (newJob.getId() % 10 == 2) {
                remainingSp[GameConstants.getSkillBook(newJob.getId())] += 2;
            }
        }
        if (newJob.getId() % 10 > 1) {
            this.remainingAp += 5;
        }
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {
            maxhp += Randomizer.rand(200, 250);
        } else if (job_ == 200) {
            maxmp += Randomizer.rand(100, 150);
        } else if (job_ % 100 == 0) {
            maxhp += Randomizer.rand(100, 150);
            maxhp += Randomizer.rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {
            maxhp += Randomizer.rand(300, 350);
        } else if (job_ < 300) {
            maxmp += Randomizer.rand(450, 500);
        } //handle KoC here (undone)
        else if (job_ > 0 && job_ != 1000) {
            maxhp += Randomizer.rand(300, 350);
            maxmp += Randomizer.rand(150, 200);
        }
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        if (!isGM()) {
            for (byte i = 1; i < 5; i++) {
                gainSlots(i, 4, true);
            }
        }
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        statup.add(new Pair<>(MapleStat.JOB, job.getId()));
        if (dragon != null) {
            getMap().broadcastMessage(MaplePacketCreator.removeDragon(dragon.getObjectId()));
            dragon = null;
        }
        recalcLocalStats();
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        silentPartyUpdate();
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.jobMessage(0, job.getId(), name), this.getId());
        }
        setMasteries(this.job.getId());
        guildUpdate();
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
        if (GameConstants.hasSPTable(newJob) && newJob.getId() != 2001) {
            if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
            }
        	createDragon();
        }
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(key, keybinding);
        } else {
            keymap.remove(key);
        }
    }

    public void changeMap(int map) {
        changeMap(map, 0);
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap;
        if (getEventInstance() != null) {
            warpMap = getEventInstance().getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap;
        if (getEventInstance() != null) {
            warpMap = getEventInstance().getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap;
        if (getEventInstance() != null) {
            warpMap = getEventInstance().getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this));//Position :O (LEFT)
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap map_ = client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

    private void changeMapInternal(final MapleMap to, final Point pos, final byte[] warpPacket) {
        if (this.getTrade() != null) {
			MapleTrade.cancelTrade(this);
		}
        client.announce(warpPacket);
        map.removePlayer(MapleCharacter.this);
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            map = to;
            setPosition(pos);
            map.addPlayer(MapleCharacter.this);
            if (party != null) {
                mpc.setMapId(to.getId());
                silentPartyUpdate();
                client.announce(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                updatePartyMemberHP();
            }
            if (getMap().getHPDec() > 0) {
                hpDecreaseTask = TimerManager.getInstance().schedule(this::doHurtHp, 10000);
            }
        }
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(Skill skill, byte newLevel, int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            if (!GameConstants.isHiddenSkills(skill.getId())) {
                this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, expiration));
            }
        } else {
            skills.remove(skill);
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, -1)); //Shouldn't use expiration anymore :)
            try {
                Connection con = DatabaseConnection.getConnection();
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?")) {
                    ps.setInt(1, skill.getId());
                    ps.setInt(2, id);
                    ps.execute();
                }
            } catch (SQLException ex) {
                System.out.print("Error deleting skill: " + ex);
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk() {
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        if (job.equals(MapleJob.DARKKNIGHT)) {
            Skill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
            final int skilllevel = getSkillLevel(BerserkX);
            if (skilllevel > 0) {
                Berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
                BerserkSchedule = TimerManager.getInstance().register(() -> {
                    client.announce(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                }, 5000, 3000);
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            World worldz = Server.getInstance().getWorld(world);
            worldz.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
            worldz.updateMessenger(getMessenger().getId(), name, client.getChannel());
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void clearDoors() {
        doors.clear();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.announce(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public int countItem(int itemid) {
        return inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            Skill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            announce(MaplePacketCreator.skillCooldown(Corsair.BATTLE_SHIP, cooldown));
            addCooldown(Corsair.BATTLE_SHIP, System.currentTimeMillis(), cooldown, TimerManager.getInstance().schedule(new CancelCooldownAction(this, Corsair.BATTLE_SHIP), cooldown * 1000));
            removeCooldown(5221999);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } else {
            announce(MaplePacketCreator.skillCooldown(5221999, battleshipHp / 10));   //:D
            addCooldown(5221999, 0, battleshipHp, null);
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?")) {
                ps.setInt(1, guildId);
                ps.execute();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?")) {
                ps.setInt(1, id);
                ps.execute();
            }
        } catch (SQLException ex) {
            System.out.print("Error deleting guild: " + ex);
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cid);
            ps.executeUpdate();
        }
    }

    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.RECOVERY) {
                        if (recoveryTask != null) {
                            recoveryTask.cancel(false);
                            recoveryTask = null;
                        }
                    } else if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(MaplePacketCreator.removeSummon(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (cancelEffectCancelTasks.schedule != null) {
                    cancelEffectCancelTasks.schedule.cancel(false);
                    this.cancelEffect(cancelEffectCancelTasks.effect, false, -1);
                }
            }
        }
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager.getInstance().schedule(() -> canDoor = true, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildrank != 1) {
            return;
        }
        try {
            Server.getInstance().disbandGuild(guildid);
        } catch (Exception ignored) {
        }
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<>(effects.values())) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public final List<PlayerDiseaseValueHolder> getAllDiseases() {
        final List<PlayerDiseaseValueHolder> ret = new ArrayList<>(5);

        DiseaseValueHolder vh;
        for (Entry<MapleDisease, DiseaseValueHolder> disease : diseases.entrySet()) {
            vh = disease.getValue();
            ret.add(new PlayerDiseaseValueHolder(disease.getKey(), vh.startTime, vh.length));
        }
        return ret;
    }

    public final boolean hasDisease(final MapleDisease dis) {
        for (final MapleDisease disease : diseases.keySet()) {
            if (disease == dis) {
                return true;
            }
        }
        return false;
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill) {
        final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<>(disease, skill.getX()));

        if (!hasDisease(disease) && diseases.size() < 2) {
            if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)) {
                if (isActiveBuffedValue(Bishop.HOLY_SHIELD)) {
                    return;
                }
            }
            TimerManager.getInstance().schedule(() -> dispelDebuff(disease), skill.getDuration());

            diseases.put(disease, new DiseaseValueHolder(System.currentTimeMillis(), skill.getDuration()));
            client.announce(MaplePacketCreator.giveDebuff(debuff, skill));
            map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, debuff, skill), false);
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            long mask = debuff.getValue();
            announce(MaplePacketCreator.cancelDebuff(mask));
            map.broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);

            diseases.remove(debuff);
        }
    }

    public void dispelDebuffs() {
        dispelDebuff(MapleDisease.CURSE);
        dispelDebuff(MapleDisease.DARKNESS);
        dispelDebuff(MapleDisease.POISON);
        dispelDebuff(MapleDisease.SEAL);
        dispelDebuff(MapleDisease.WEAKEN);
    }

    public void cancelAllDebuffs() {
        diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null) {
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(this::doHurtHp, 10000);
    }

    public void dropMessage(String message) {
        dropMessage(0, message);
    }

    public void dropMessage(int type, String message) {
        client.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public List<ScheduledFuture<?>> getTimers() {
        return timers;
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<>(MapleStat.MP, getMp()));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<>(MapleStat.HP, getHp()));
        }
        if (stats.size() > 0) {
            client.announce(MaplePacketCreator.updatePlayerStats(stats, this));
        }
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getMessenger() != null) {
            Server.getInstance().getWorld(world).updateMessenger(getMessenger(), getName(), getWorld(), client.getChannel());
        }
    }

    public void cancelExpirationTask() {
        if (expiretask != null) {
            expiretask.cancel(false);
            expiretask = null;
        }
    }

    public void expirationTask() {
        if (expiretask == null) {
            expiretask = TimerManager.getInstance().register(() -> {
                long expiration, currenttime = System.currentTimeMillis();
                Set<Skill> keys = getSkills().keySet();
                for (Skill key : keys) {
                    SkillEntry skill = getSkills().get(key);
                    if (skill.expiration != -1 && skill.expiration < currenttime) {
                        changeSkillLevel(key, (byte) -1, 0, -1);
                    }
                }

                List<Item> toberemove = new ArrayList<>();
                for (MapleInventory inv : inventory) {
                    for (Item item : inv.list()) {
                        expiration = item.getExpiration();
                        if (expiration != -1 && (expiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                            byte aids = item.getFlag();
                            aids &= ~(ItemConstants.LOCK);
                            item.setFlag(aids); //Probably need a check, else people can make expiring items into permanent items...
                            item.setExpiration(-1);
                            forceUpdateItem(item);   //TEST :3
                        } else if (expiration != -1 && expiration < currenttime) {
                            client.announce(MaplePacketCreator.itemExpired(item.getItemId()));
                            toberemove.add(item);
                        }
                    }
                    for (Item item : toberemove) {
                        MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
                    }
                    toberemove.clear();
                }
            }, 60000);
        }
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void forceUpdateItem(Item item) {
        final List<ModifyInventory> mods = new LinkedList<>();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        client.announce(MaplePacketCreator.modifyInventory(true, mods));
    }

    public void gainGachaExp() {
        int expgain = 0;
        int currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)) {
            expgain += ExpTable.getExpNeededForLevel(level) - exp.get();
            int nextneed = ExpTable.getExpNeededForLevel(level + 1);
            if ((currentgexp - expgain) >= nextneed) {
                expgain += nextneed;
            }
            this.gachaexp.set(currentgexp - expgain);
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, false);
        updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
    }

    public void gainGachaExp(int gain) {
        updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, 0, show, inChat, true);
    }

    public void gainExp(int gain, int party, boolean show, boolean inChat, boolean white) {
        if (hasDisease(MapleDisease.CURSE)) {
			gain *= 0.5;
            party *= 0.5;
        }
		
        int equip = (gain / 10) * pendantExp;
        int total = gain + equip + party;

        if (level < getMaxLevel()) {
            if ((long) this.exp.get() + (long) total > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
                total -= gainFirst + 1;
                this.gainExp(gainFirst + 1, party, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(total));
            if (show && gain != 0) {
                client.announce(MaplePacketCreator.getShowExpGain(gain, equip, party, inChat, white));
            }
            if (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp(true);
                int need = ExpTable.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, need);
                }
            }
        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        if (meso.get() + gain < 0) {
            client.announce(MaplePacketCreator.enableActions());
            return;
        }
        updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        if (show) {
            client.announce(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    public void genericGuildMessage(int code) {
        this.client.announce(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

	public List<Pair<MapleBuffStat, Integer>> getAllStatups() {
        List<Pair<MapleBuffStat, Integer>> ret = new ArrayList<>();
        for (MapleBuffStat mbs : effects.keySet()) {
            MapleBuffStatValueHolder mbsvh = effects.get(mbs);
			ret.add(new Pair<>(mbs, mbsvh.value));
        }
        return ret;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<>();

        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `id`, `accountid`, `name` FROM `characters` WHERE `name` = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.startTime;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.value;
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    public MapleStatEffect getBuffEffect(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return null;
        } else {
            return mbsvh.effect;
        }
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

    public int getChair() {
        return chair;
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public boolean getDojoParty() {
        return dojoParty;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<>(doors);
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getEnergyBar() {
        return energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public ArrayList<Integer> getExcluded() {
        return excluded;
    }

    public int getExp() {
        return exp.get();
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public int getExpRate() {
        return expRate;
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamily getFamily() {
        return family;
    }

    public void setFamily(MapleFamily f) {
        this.family = f;
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public boolean isMale() {
        return getGender() == 0;
    }

    public MapleGuild getGuild() {
        try {
            return Server.getInstance().getGuild(getGuildId(), getWorld(), null);
        } catch (Exception ex) {
            return null;
        }
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public int getHair() {
        return hair;
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMpApUsed() {
        return hpMpApUsed;
    }

    public int getId() {
        return id;
    }

    public static int getIdByName(String name) {
        try {
            int id;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return -1;
                    }
                    id = rs.getInt("id");
                }
            }
            return id;
        } catch (Exception ignored) {
        }
        return -1;
    }

    public static String getNameById(int id) {
        try {
            String name;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }
                    name = rs.getString("name");
                }
            }
            return name;
        } catch (Exception ignored) {
        }
        return null;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public int getLevel() {
        return level;
    }

    public int getLuk() {
        return luk;
    }

    public int getFh() {
		Point pos = this.getPosition();
		pos.y -= 6;
        if (getMap().getFootholds().findBelow(pos) == null) {
            return 0;
        } else {
            return getMap().getFootholds().findBelow(pos).getY1();
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public int getMarkedMonster() {
        return markedMonster;
    }

    public MapleRing getMarriageRing() {
        return marriageRing;
    }

    public int getMarried() {
        return married;
    }

    public int getMasterLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMaxLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMerchantMeso() {
        return merchantmeso;
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public MaplePartyCharacter getMPC() {
        if (mpc == null) {
            mpc = new MaplePartyCharacter(this);
        }
        return mpc;
    }

    public void setMPC(MaplePartyCharacter mpc) {
        this.mpc = mpc;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public int getMiniGamePoints(String type, boolean omok) {
        if (omok) {
            switch (type) {
                case "wins":
                    return omokwins;
                case "losses":
                    return omoklosses;
                default:
                    return omokties;
            }
        } else {
            switch (type) {
                case "wins":
                    return matchcardwins;
                case "losses":
                    return matchcardlosses;
                default:
                    return matchcardties;
            }
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public byte getPetIndex(int petId) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public byte getPetIndex(MaplePet pet) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        for (final MapleQuestStatus q : quests.values()) {
            if (q.getQuest().getId() == quest) {
                return (byte) q.getStatus().getId();
            }
        }
        return 0;
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest.getId())) {
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest.getId());
    }

    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) {
            return true; //For non quest items :3
        }
        MapleQuest quest = MapleQuest.getInstance(questid);
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) < quest.getItemAmountNeeded(itemid);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp[GameConstants.getSkillBook(job.getId())]; //default
    }

    public int getRemainingSpBySkill(final int skillbook) {
        return remainingSp[skillbook];
    }

    public int[] getRemainingSps() {
        return remainingSp;
    }

    public int getSavedLocation(String type) {
        SavedLocation sl = savedLocations[SavedLocationType.fromString(type).ordinal()];
        if (sl == null) {
            return -1;
        }
        int m = sl.getMapId();
        if (!SavedLocationType.fromString(type).equals(SavedLocationType.WORLDTOUR)) {
            clearSavedLocation(SavedLocationType.fromString(type));
        }
        return m;
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(Skill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return -1;
        }
        return ret.expiration;
    }

    public long getSkillExpiration(Skill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public final int getStartedQuestsSize() {
        int i = 0;
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                if (q.getQuest().getInfoNumber() > 0) {
                    i++;
                }
                i++;
            }
        }
        return i;
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public int getWorld() {
        return world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        if (skillid == 5221999) {
            this.battleshipHp = (int) length;
            addCooldown(skillid, 0, length, null);
        } else {
            int time = (int) ((length + starttime) - System.currentTimeMillis());
            addCooldown(skillid, System.currentTimeMillis(), time, TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time));
        }
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    private void guildUpdate() {
        if (this.guildid < 1) {
            return;
        }
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        try {
            Server.getInstance().memberLevelJobUpdate(this.mgc);
            //Server.getInstance().getGuild(guildid, world, mgc).gainGP(40);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                Server.getInstance().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has to be > 0
        Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        TimerManager tMan = TimerManager.getInstance();
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, energybar));
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
            client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
            client.announce(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(energybar, stat));
        }
        if (energybar >= 10000 && energybar < 11000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            tMan.schedule(() -> {
                energybar = 0;
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ENERGY_CHARGE, energybar));
                setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
                client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat));
                getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignBuff(energybar, stat));
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
        Skill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.announce(MaplePacketCreator.giveBuff(skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(to.getId());
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")) {
                ps.setInt(1, getId());
                ps.setInt(2, to.getId());
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, false) > 0;
    }

    public void increaseGuildCapacity() { //hopefully nothing is null
        if (getMeso() < getGuild().getIncreaseGuildCost(getGuild().getCapacity())) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        Server.getInstance().increaseGuildCapacity(guildid);
        gainMeso(-getGuild().getIncreaseGuildCost(getGuild().getCapacity()), true, false, false);
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, Skill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh != null && mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return getJob().getId() >= 2000 && getJob().getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (getJob().getId() == 0 || getJob().getId() == 1000 || getJob().getId() == 2000) && getLevel() < 11;
    }

    public boolean isGM() {
        return gmLevel > -1;
        // todo: for testing purposes, everyone is a gm right now.
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        return party.getLeader() == party.getMemberById(getId());
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        if (chair != 0) {
            chair = 0;
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
    }

    public void levelUp(boolean takeexp) {
        Skill improvingMaxHP = null;
        Skill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;

        if (isBeginnerJob()) {
            remainingAp = 0;
            if (getLevel() < 6) {
                str += 5;
            } else {
                str += 4;
                dex += 1;
            }
        } else {
            remainingAp += 5;
            if (isCygnus() && level < 70) {
                remainingAp++;
            }
        }
        if (job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE || job == MapleJob.LEGEND) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
            if (job.isA(MapleJob.CRUSADER)) {
                improvingMaxMP = SkillFactory.getSkill(1210000);
            } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
                improvingMaxMP = SkillFactory.getSkill(11110000);
            }
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 30000;
            maxmp = 30000;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(22, 28);
            maxmp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.ARAN1)) {
            maxhp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            maxmp += aids + Math.floor(aids * 0.1);
        }
        if (improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1))) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1))) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += localint_ / 10;
        if (takeexp) {
            exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }
        level++;
        if (level >= getMaxLevel()) {
            exp.set(0);
            level = getMaxLevel(); //To prevent levels past 200
        }
        if (level % 50 == 0) { //For the drop + meso rate
            setRates();
        }
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        if (level == 200) {
            exp.set(0);
        }
        hp = maxhp;
        mp = maxmp;
        recalcLocalStats();
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.HP, localmaxhp));
        statup.add(new Pair<>(MapleStat.MP, localmaxmp));
        statup.add(new Pair<>(MapleStat.EXP, exp.get()));
        statup.add(new Pair<>(MapleStat.LEVEL, level));
        statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<>(MapleStat.STR, str));
        statup.add(new Pair<>(MapleStat.DEX, dex));
        if (job.getId() % 1000 > 0) {
        	remainingSp[GameConstants.getSkillBook(job.getId())] += 3;
        	statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        }
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        recalcLocalStats();
        setMPC(new MaplePartyCharacter(this));
        silentPartyUpdate();
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.levelUpMessage(2, level, name), this.getId());
        }
        if (ServerConstants.PERFECT_PITCH && level >= 30) {
            //milestones?
            if (MapleInventoryManipulator.checkSpace(client, 4310000, (short) 1, "")) {
                MapleInventoryManipulator.addById(client, 4310000, (short) 1);
            }
        }
        if (level == 200 && !isGM()) {
            final String names = (getMedalText() + name);
            client.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, String.format(LEVEL_200, names, names)));
        }
        showLevelupMessage();
        guildUpdate();
    }

    public void gainAp(int amount) {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(1);
        remainingAp += amount;
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    public void gainSp(int amount) {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(1);
        remainingSp[GameConstants.getSkillBook(job.getId())] += amount;
        statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp[GameConstants.getSkillBook(job.getId())]));
        client.announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    private void showLevelupMessage() {
        if (level % 5 != 0) { //Performance FTW?
            return;
        }

        String[] messages = new String[] {
                "Aww, you're level 5, how cute!",
                "Henesys Party Quest is now open to you! Head over to Henesys, find some friends, and try it out!",
                "Half-way to your 2nd job advancement, nice work!",
                "You can almost Kerning Party Quest!",
                "You seem to be improving, but you are still not ready to move on to the next step.",
                "You have finally reached level 30! Try job advancing, after that try the Mushroom Kingdom!",
                "Hey did you hear about this mall that opened in Kerning? Try visiting the Kerning Mall.",
                "Do @rates to see what all your rates are!",
                "I heard that a rock and roll artist died during the grand opening of the Kerning Mall. People are naming him the Spirit of Rock.",
                "You seem to be growing very fast, would you like to test your new found strength with the mighty Zakum?",
                "You can now try out the Ludibrium Maze Party Quest!",
                "Feels good to be near the end of 2nd job, doesn't it?",
                "You're only 5 more levels away from 3rd job, not bad!",
                "I see many people wearing a teddy bear helmet. I should ask someone where they got it from.",
                "You have reached level 3 quarters!",
                "You think you are powerful enough? Try facing horntail!",
                "Did you know? The majority of people who hit level 85 in Solaxia don't live to be 85 years old?",
                "Hey do you like the amusement park? I heard Spooky Wood is the best theme park around. I heard they sell cute teddy-bears.",
                "100% of people who hit level 95 in Solaxia don't live to be 95 years old.",
                "Your drop rate has increased to 3x since you have reached level 100!",
                "Have you ever been to leafre? I heard they have dragons!",
                "I see many people wearing a teddy bear helmet. I should ask someone where they got it from.",
                "I bet all you can think of is level 120, huh? Level 115 gets no love.",
                "Are you ready to learn from the masters? Head over to your job instructor!",
                "The struggle for mastery books has begun, huh?",
                "You should try Temple of Time. It should be pretty decent EXP.",
                "I hope you're still not struggling for mastery books!",
                "You're well into 4th job at this point, great work!",
                "Level 145 is serious business!",
                "You have becomed quite strong, but the journey is not yet over.",
                "At level 155, Zakum should be a joke to you. Nice job!",
                "Level 160 is pretty impressive. Try taking a picture and putting it on Instagram.",
                "At this level, you should start looking into doing some boss runs.",
                "Level 170, huh? You have the heart of a champion.",
                "You came a long way from level 1. Amazing job so far.",
                "Have you ever tried taking a boss on by yourself? It is quite difficult.",
                "Legend has it that you're a legend.",
                "You only have 10 more levels to go until you hit 200!",
                "Nothing is stopping you at this point, level 195!",
                "Your drop rate has increased to 4x since you have reached level 200!"
        };
        HashMap<Integer, String> map = new HashMap<>();
        int count = 0;
        for (int i = 5; i < 200; i+=5) {
            map.put(i, messages[count]);
            count ++;
        }
        yellowMessage(map.get(level));
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        try {
            MapleCharacter result = new MapleCharacter();
            result.client = client;
            result.id = charid;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("Loading char failed (not found)");
            }
            result.name = rs.getString("name");
            result.level = rs.getInt("level");
            result.fame = rs.getInt("fame");
            result.str = rs.getInt("str");
            result.dex = rs.getInt("dex");
            result.int_ = rs.getInt("int");
            result.luk = rs.getInt("luk");
            result.exp.set(rs.getInt("exp"));
            result.gachaexp.set(rs.getInt("gachaexp"));
            result.hp = rs.getInt("hp");
            result.maxhp = rs.getInt("maxhp");
            result.mp = rs.getInt("mp");
            result.maxmp = rs.getInt("maxmp");
            result.hpMpApUsed = rs.getInt("hpMpUsed");
            result.hasMerchant = rs.getInt("HasMerchant") == 1;
            String[] skillPoints = rs.getString("sp").split(",");
            for (int i = 0; i < result.remainingSp.length; i++) {
                result.remainingSp[i] = Integer.parseInt(skillPoints[i]);
            }
            result.remainingAp = rs.getInt("ap");
            result.meso.set(rs.getInt("meso"));
            result.merchantmeso = rs.getInt("MerchantMesos");
            result.gmLevel = rs.getInt("gm");
            result.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
            result.gender = rs.getInt("gender");
            result.job = MapleJob.getById(rs.getInt("job"));
            result.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
            result.vanquisherKills = rs.getInt("vanquisherKills");
            result.omokwins = rs.getInt("omokwins");
            result.omoklosses = rs.getInt("omoklosses");
            result.omokties = rs.getInt("omokties");
            result.matchcardwins = rs.getInt("matchcardwins");
            result.matchcardlosses = rs.getInt("matchcardlosses");
            result.matchcardties = rs.getInt("matchcardties");
            result.hair = rs.getInt("hair");
            result.face = rs.getInt("face");
            result.accountid = rs.getInt("accountid");
            result.mapid = rs.getInt("map");
            result.initialSpawnPoint = rs.getInt("spawnpoint");
            result.world = rs.getByte("world");
            result.rank = rs.getInt("rank");
            result.rankMove = rs.getInt("rankMove");
            result.jobRank = rs.getInt("jobRank");
            result.jobRankMove = rs.getInt("jobRankMove");
            int mountexp = rs.getInt("mountexp");
            int mountlevel = rs.getInt("mountlevel");
            int mounttiredness = rs.getInt("mounttiredness");
            result.guildid = rs.getInt("guildid");
            result.guildrank = rs.getInt("guildrank");
            result.allianceRank = rs.getInt("allianceRank");
            result.familyId = rs.getInt("familyId");
            result.bookCover = rs.getInt("monsterbookcover");
            result.monsterbook = new MonsterBook();
            result.monsterbook.loadCards(charid);
            result.vanquisherStage = rs.getInt("vanquisherStage");
            result.dojoPoints = rs.getInt("dojoPoints");
            result.dojoStage = rs.getInt("lastDojoStage");
            result.dataString = rs.getString("dataString");
            if (result.guildid > 0) {
                result.mgc = new MapleGuildCharacter(result);
            }
            int buddyCapacity = rs.getInt("buddyCapacity");
            result.buddylist = new BuddyList(buddyCapacity);
            result.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
            result.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("useslots"));
            result.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
            result.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));
            for (Pair<Item, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(result.id, !channelserver)) {
                result.getInventory(item.getRight()).addFromDB(item.getLeft());
                Item itemz = item.getLeft();
                if (itemz.getPetId() > -1) {
                    MaplePet pet = itemz.getPet();
                    if (pet != null && pet.isSummoned()) {
                        result.addPet(pet);
                    }
                    continue;
                }
                if (item.getRight().equals(MapleInventoryType.EQUIP) || item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                    Equip equip = (Equip) item.getLeft();
                    if (equip.getRingId() > -1) {
                        MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        if (item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                            ring.equip();
                        }
                        if (ring.getItemId() > 1112012) {
                            result.addFriendshipRing(ring);
                        } else {
                            result.addCrushRing(ring);
                        }
                    }
                }
            }
            if (channelserver) {
                MapleMapFactory mapFactory = client.getChannelServer().getMapFactory();
                result.map = mapFactory.getMap(result.mapid);
                if (result.map == null) {
                    result.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = result.map.getPortal(result.initialSpawnPoint);
                if (portal == null) {
                    portal = result.map.getPortal(0);
                    result.initialSpawnPoint = 0;
                }
                result.setPosition(portal.getPosition());
                int partyid = rs.getInt("party");
                MapleParty party = Server.getInstance().getWorld(result.world).getParty(partyid);
                if (party != null) {
                    result.mpc = party.getMemberById(result.id);
                    if (result.mpc != null) {
                        result.party = party;
                    }
                }
                int messengerid = rs.getInt("messengerid");
                int position = rs.getInt("messengerposition");
                if (messengerid > 0 && position < 4 && position > -1) {
                    MapleMessenger messenger = Server.getInstance().getWorld(result.world).getMessenger(messengerid);
                    if (messenger != null) {
                        result.messenger = messenger;
                        result.messengerposition = position;
                    }
                }
                result.loggedIn = true;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 15");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            byte v = 0;
            byte r = 0;
            while (rs.next()) {
                if (rs.getInt("vip") == 1) {
                    result.viptrockmaps.add(rs.getInt("mapid"));
                    v++;
                } else {
                    result.trockmaps.add(rs.getInt("mapid"));
                    r++;
                }
            }
            while (v < 10) {
                result.viptrockmaps.add(999999999);
                v++;
            }
            while (r < 5) {
                result.trockmaps.add(999999999);
                r++;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, result.accountid);
            rs = ps.executeQuery();
            if (rs.next()) {
                result.getClient().setAccountName(rs.getString("name"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `area`,`info` FROM area_info WHERE charid = ?");
            ps.setInt(1, result.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.area_info.put(rs.getShort("area"), rs.getString("info"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `name`,`info` FROM eventstats WHERE characterid = ?");
            ps.setInt(1, result.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                if (rs.getString("name").equals("rescueGaga")) {
                    result.events.put(name, new RescueGaga(rs.getInt("info")));
                }
                //ret.events = new MapleEvents(new RescueGaga(rs.getInt("rescuegaga")), new ArtifactHunt(rs.getInt("artifacthunt")));
            }
            rs.close();
            ps.close();
            result.cashshop = new CashShop(result.accountid, result.id, result.getJobType());
            result.autoban = new AutobanManager(result);
            result.marriageRing = null; //for now
            ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountid = ? AND id != ? ORDER BY level DESC limit 1");
            ps.setInt(1, result.accountid);
            ps.setInt(2, charid);
            rs = ps.executeQuery();
            if (rs.next()) {
                result.linkedName = rs.getString("name");
                result.linkedLevel = rs.getInt("level");
            }
            rs.close();
            ps.close();
            if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                PreparedStatement psf;
                try (PreparedStatement pse = con.prepareStatement("SELECT * FROM questprogress WHERE queststatusid = ?")) {
                    psf = con.prepareStatement("SELECT mapid FROM medalmaps WHERE queststatusid = ?");
                    while (rs.next()) {
                        MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
                        MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                        long cTime = rs.getLong("time");
                        if (cTime > -1) {
                            status.setCompletionTime(cTime * 1000);
                        }
                        status.setForfeited(rs.getInt("forfeited"));
                        result.quests.put(q.getId(), status);
                        pse.setInt(1, rs.getInt("queststatusid"));
                        try (ResultSet rsProgress = pse.executeQuery()) {
                            while (rsProgress.next()) {
                                status.setProgress(rsProgress.getInt("progressid"), rsProgress.getString("progress"));
                            }
                        }
                        psf.setInt(1, rs.getInt("queststatusid"));
                        try (ResultSet medalmaps = psf.executeQuery()) {
                            while (medalmaps.next()) {
                                status.addMedalMap(medalmaps.getInt("mapid"));
                            }
                        }
                    }
                    rs.close();
                    ps.close();
                }
                psf.close();
                ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel,expiration FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    result.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?");
                ps.setInt(1, result.getId());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int skillid = rs.getInt("SkillID");
                    final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                    if (skillid != 5221999 && (length + startTime < System.currentTimeMillis())) {
                        continue;
                    }
                    result.giveCoolDowns(skillid, startTime, length);
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?");
                ps.setInt(1, result.getId());
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt("position");
                    SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                    result.skillMacros[position] = macro;
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int key = rs.getInt("key");
                    int type = rs.getInt("type");
                    int action = rs.getInt("action");
                    result.keymap.put(key, new MapleKeyBinding(type, action));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    result.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                result.lastfametime = 0;
                result.lastmonthfameids = new ArrayList<>(31);
                while (rs.next()) {
                    result.lastfametime = Math.max(result.lastfametime, rs.getTimestamp("when").getTime());
                    result.lastmonthfameids.add(rs.getInt("characterid_to"));
                }
                rs.close();
                ps.close();
                result.buddylist.loadFromDb(charid);
                result.storage = MapleStorage.loadOrCreateFromDB(result.accountid, result.world);
                result.recalcLocalStats();
                //ret.resetBattleshipHp();
                result.silentEnforceMaxHpMp();
            }
            int mountid = result.getJobType() * 10000000 + 1004;
            if (result.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null) {
                result.maplemount = new MapleMount(result, result.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18).getItemId(), mountid);
            } else {
                result.maplemount = new MapleMount(result, 0, mountid);
            }
            result.maplemount.setExp(mountexp);
            result.maplemount.setLevel(mountlevel);
            result.maplemount.setTiredness(mounttiredness);
            result.maplemount.setActive(false);
            return result;
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;

    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    private static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime, length;
        public ScheduledFuture<?> timer;

        MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public void message(String m) {
        dropMessage(5, m);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }

    public void mobKilled(int id) {
        // It seems nexon uses monsters that don't exist in the WZ (except string) to merge multiple mobs together for these 3 monsters.
        // We also want to run mobKilled for both since there are some quest that don't use the updated ID...
        if (id == 1110100 || id == 1110130) {
            mobKilled(9101000);
        } else if (id == 2230101 || id == 2230131) {
            mobKilled(9101001);
        } else if (id == 1140100 || id == 1140130) {
            mobKilled(9101002);
        }
		int lastQuestProcessed = 0;
        try {
            for (MapleQuestStatus q : quests.values()) {
				lastQuestProcessed = q.getQuest().getId();
                if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                    continue;
                }
                String progress = q.getProgress(id);
                if (!progress.isEmpty() && Integer.parseInt(progress) >= q.getQuest().getMobAmountNeeded(id)) {
                    continue;
                }
                if (q.progress(id)) {
                    client.announce(MaplePacketCreator.updateQuest(q, false));
                }
            }
        } catch (Exception e) {
			FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, e, "MapleCharacter.mobKilled. CID: " + this.id + " last Quest Processed: " + lastQuestProcessed);
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public void playerNPC(MapleCharacter v, int scriptId) {
        int npcId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE ScriptId = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, v.getName());
                ps.setInt(2, v.getHair());
                ps.setInt(3, v.getFace());
                ps.setInt(4, v.getSkinColor().getId());
                ps.setInt(5, getPosition().x);
                ps.setInt(6, getPosition().y);
                ps.setInt(7, getMapId());
                ps.setInt(8, scriptId);
                ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
                ps.setInt(10, getPosition().x + 50);
                ps.setInt(11, getPosition().x - 50);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                npcId = rs.getInt(1);
                ps.close();
                ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (Item equip : getInventory(MapleInventoryType.EQUIPPED)) {
                    int position = Math.abs(equip.getPosition());
                    if ((position < 12 && position > 0) || (position > 100 && position < 112)) {
                        ps.setInt(2, equip.getItemId());
                        ps.setInt(3, equip.getPosition());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                ps.setInt(1, scriptId);
                rs = ps.executeQuery();
                rs.next();
                PlayerNPCs pn = new PlayerNPCs(rs);
                for (Channel channel : Server.getInstance().getChannelsFromWorld(world)) {
                    MapleMap m = channel.getMapFactory().getMap(getMapId());
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playerDead() {
        cancelAllBuffs(false);
        dispelDebuffs();
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (mapid > 925020000 && mapid < 925030000) {
            this.dojoStage = 0;
        } else if (mapid > 980000100 && mapid < 980000700) {
            getMap().broadcastMessage(this, MaplePacketCreator.CPQDied(this));
        } else if (getJob() != MapleJob.BEGINNER) { //Hmm...
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown()) {
                XPdummy /= 100;
            }
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel())) {
                if (getLuk() <= 100 && getLuk() > 8) {
                    XPdummy *= (200 - getLuk()) / 2000;
                } else if (getLuk() < 8) {
                    XPdummy /= 10;
                } else {
                    XPdummy /= 20;
                }
            }
            if (getExp() > XPdummy) {
                gainExp(-XPdummy, false, false);
            } else {
                gainExp(-getExp(), false, false);
            }
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }

        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        if (getChair() == -1) {
            setChair(0);
            client.announce(MaplePacketCreator.cancelChair(-1));
            getMap().broadcastMessage(this, MaplePacketCreator.showChair(getId(), 0), false);
        }
        client.announce(MaplePacketCreator.enableActions());
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(() -> {
            addHP(-bloodEffect.getX());
            client.announce(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
            getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
            checkBerserk();
        }, 4000, 4000);
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100, jump = 100;
        magic = localint_;
        watk = 0;

        for (Item item : getInventory(MapleInventoryType.EQUIPPED)) {
            Equip equip = (Equip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff;
        }
        MapleStatEffect combo = getBuffEffect(MapleBuffStat.ARAN_COMBO);
        if (combo != null) {
            watk += combo.getX();
        }

        if (energybar == 15000) {
            Skill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
            MapleStatEffect ceffect = energycharge.getEffect(getSkillLevel(energycharge));
            watk += ceffect.getWatk();
        }

        Integer mwarr = getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (mwarr != null) {
            localstr += getStr() * mwarr / 100;
            localdex += getDex() * mwarr / 100;
            localint_ += getInt() * mwarr / 100;
            localluk += getLuk() * mwarr / 100;
        }
        if (job.isA(MapleJob.BOWMAN)) {
            Skill expert = null;
            if (job.isA(MapleJob.MARKSMAN)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff;
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff;
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff;
        }

        Integer blessing = getSkillLevel(10000000 * getJobType() + 12);
        if (blessing > 0) {
            watk += blessing;
            magic += blessing * 2;
        }

        if (job.isA(MapleJob.THIEF) || job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.NIGHTWALKER1) || job.isA(MapleJob.WINDARCHER1)) {
            Item weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                boolean bow = weapon == MapleWeaponType.BOW;
                boolean crossbow = weapon == MapleWeaponType.CROSSBOW;
                boolean claw = weapon == MapleWeaponType.CLAW;
                boolean gun = weapon == MapleWeaponType.GUN;
                if (bow || crossbow || claw || gun) {
                    // Also calc stars into this.
                    MapleInventory inv = getInventory(MapleInventoryType.USE);
                    for (short i = 1; i <= inv.getSlotLimit(); i++) {
                        Item item = inv.getItem(i);
                        if (item != null) {
                            if ((claw && ItemConstants.isThrowingStar(item.getItemId())) || (gun && ItemConstants.isBullet(item.getItemId())) || (bow && ItemConstants.isArrowForBow(item.getItemId())) || (crossbow && ItemConstants.isArrowForCrossBow(item.getItemId()))) {
                                if (item.getQuantity() > 0) {
                                    // Finally there!
                                    watk += MapleItemInformationProvider.getInstance().getWatkForProjectile(item.getItemId());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // Add throwing stars to dmg.
        }

        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        client.announce(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
        if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isBeholder()) {
            final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            Skill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(() -> {
                    addHP(healEffect.getHp());
                    client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                }, healInterval, healInterval);
            }
            Skill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(() -> {
                    buffEffect.applyTo(MapleCharacter.this);
                    client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), beholder, 2), false);
                }, buffInterval, buffInterval);
            }
        } else if (effect.isRecovery()) {
            final byte heal = (byte) effect.getX();
            recoveryTask = TimerManager.getInstance().register(() -> {
                addHP(heal);
                client.announce(MaplePacketCreator.showOwnRecovery(heal));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, heal), false);
            }, 5000, 5000);
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight()));
        }
        recalcLocalStats();
    }

    public void removeAllCooldownsExcept(int id, boolean packet) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values()) {
            if (mcvh.skillId != id) {
                coolDowns.remove(mcvh.skillId);
                if (packet) {
                    client.announce(MaplePacketCreator.skillCooldown(mcvh.skillId, 0));
                }
            }
        }
    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId)) {
            this.coolDowns.remove(skillId);
        }
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void resetStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        int tap = 0, tsp = 1;
        int tstr = 4, tdex = 4, tint = 4, tluk = 4;
        int levelap = (isCygnus() ? 6 : 5);
        switch (job.getId()) {
            case 100:
            case 1100:
            case 2100://?
                tstr = 35;
                tap = ((getLevel() - 10) * levelap) + 14;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 200:
            case 1200:
                tint = 20;
                tap = ((getLevel() - 8) * levelap) + 29;
                tsp += ((getLevel() - 8) * 3);
                break;
            case 300:
            case 1300:
            case 400:
            case 1400:
                tdex = 25;
                tap = ((getLevel() - 10) * levelap) + 24;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 500:
            case 1500:
                tdex = 20;
                tap = ((getLevel() - 10) * levelap) + 29;
                tsp += ((getLevel() - 10) * 3);
                break;
        }
        this.remainingAp = tap;
        this.remainingSp[GameConstants.getSkillBook(job.getId())] = tsp;
        this.dex = tdex;
        this.int_ = tint;
        this.str = tstr;
        this.luk = tluk;
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, tap));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, tsp));
        statup.add(new Pair<>(MapleStat.STR, tstr));
        statup.add(new Pair<>(MapleStat.DEX, tdex));
        statup.add(new Pair<>(MapleStat.INT, tint));
        statup.add(new Pair<>(MapleStat.LUK, tluk));
        announce(MaplePacketCreator.updatePlayerStats(statup, this));
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        if (entered.containsKey(map.getId())) {
            entered.remove(map.getId());
        }
    }

    public void resetEnteredScript(int mapId) {
        if (entered.containsKey(mapId)) {
            entered.remove(mapId);
        }
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public void resetMGC() {
        this.mgc = null;
    }

    public synchronized void saveCooldowns() {
        if (getAllCooldowns().size() > 0) {
            try {
                Connection con = DatabaseConnection.getConnection();
                deleteWhereCharacterId(con, "DELETE FROM cooldowns WHERE charid = ?");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, getId());
                    for (PlayerCoolDownValueHolder cooling : getAllCooldowns()) {
                        ps.setInt(2, cooling.skillId);
                        ps.setLong(3, cooling.startTime);
                        ps.setLong(4, cooling.length);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void saveGuildStatus() {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?")) {
                ps.setInt(1, guildid);
                ps.setInt(2, guildrank);
                ps.setInt(3, allianceRank);
                ps.setInt(4, id);
                ps.execute();
            }
        } catch (SQLException ignored) {
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public final boolean insertNewChar() {
        final Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;

        try {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            ps = con.prepareStatement("INSERT INTO characters (str, dex, luk, `int`, gm, skincolor, gender, job, hair, face, map, meso, spawnpoint, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
            ps.setInt(1, 12);
            ps.setInt(2, 5);
            ps.setInt(3, 4);
            ps.setInt(4, 4);
            ps.setInt(5, gmLevel);
            ps.setInt(6, skinColor.getId());
            ps.setInt(7, gender);
            ps.setInt(8, getJob().getId());
            ps.setInt(9, hair);
            ps.setInt(10, face);
            ps.setInt(11, mapid);
            ps.setInt(12, Math.abs(meso.get()));
            ps.setInt(13, 0);
            ps.setInt(14, accountid);
            ps.setString(15, name);
            ps.setInt(16, world);

            int updateRows = ps.executeUpdate();
            if (updateRows < 1) {
                ps.close();
                FilePrinter.printError(FilePrinter.INSERT_CHAR, "Error trying to insert " + name);
                return false;
            }
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                FilePrinter.printError(FilePrinter.INSERT_CHAR, "Inserting char failed " + name);
                return false;
            }

            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (int i = 0; i < DEFAULT_KEY.length; i++) {
                ps.setInt(2, DEFAULT_KEY[i]);
                ps.setInt(3, DEFAULT_TYPE[i]);
                ps.setInt(4, DEFAULT_ACTION[i]);
                ps.execute();
            }
            ps.close();

            final List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

            for (MapleInventory iv : inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
            con.commit();
            return true;
        } catch (Throwable t) {
            FilePrinter.printError(FilePrinter.INSERT_CHAR, t, "Error creating " + name + " Level: " + level + " Job: " + job.getId());
            try {
                con.rollback();
            } catch (SQLException se) {
                FilePrinter.printError(FilePrinter.INSERT_CHAR, se, "Error trying to rollback " + name);
            }
            return false;
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (SQLException ignored) {
            }
        }
    }

    // synchronize this call instead of trying to give access all at once (?)
    public synchronized void saveToDB() {
        Calendar c = Calendar.getInstance();
        FilePrinter.print(FilePrinter.SAVING_CHARACTER, "Attempting to save " + name + " at " + c.getTime().toString());
        Connection con = DatabaseConnection.getConnection();
        try {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            PreparedStatement ps;
            ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, gachaexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, dataString = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            if (gmLevel < 1 && level > 199) {
                ps.setInt(1, isCygnus() ? 120 : 200);
            } else {
                ps.setInt(1, level);
            }
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, Math.abs(exp.get()));
            ps.setInt(8, Math.abs(gachaexp.get()));
            ps.setInt(9, hp);
            ps.setInt(10, mp);
            ps.setInt(11, maxhp);
            ps.setInt(12, maxmp);
            StringBuilder sps = new StringBuilder();
            for (int aRemainingSp : remainingSp) {
                sps.append(aRemainingSp);
                sps.append(",");
            }
            String sp = sps.toString();
            ps.setString(13, sp.substring(0, sp.length() - 1));
            ps.setInt(14, remainingAp);
            ps.setInt(15, gmLevel);
            ps.setInt(16, skinColor.getId());
            ps.setInt(17, gender);
            ps.setInt(18, job.getId());
            ps.setInt(19, hair);
            ps.setInt(20, face);
            if (map == null || (cashshop != null && cashshop.isOpened())) {
                ps.setInt(21, mapid);
            } else {
                if (map.getForcedReturnId() != 999999999) {
                    ps.setInt(21, map.getForcedReturnId());
                } else {
                    ps.setInt(21, getHp() < 1 ? map.getReturnMapId() : map.getId());
                }
            }
            ps.setInt(22, meso.get());
            ps.setInt(23, hpMpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {
                ps.setInt(24, 0);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(24, closest.getId());
                } else {
                    ps.setInt(24, 0);
                }
            }
            if (party != null) {
                ps.setInt(25, party.getId());
            } else {
                ps.setInt(25, -1);
            }
            ps.setInt(26, buddylist.getCapacity());
            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }
            if (maplemount != null) {
                ps.setInt(29, maplemount.getLevel());
                ps.setInt(30, maplemount.getExp());
                ps.setInt(31, maplemount.getTiredness());
            } else {
                ps.setInt(29, 1);
                ps.setInt(30, 0);
                ps.setInt(31, 0);
            }
            for (int i = 1; i < 5; i++) {
                ps.setInt(i + 31, getSlots(i));
            }

            monsterbook.saveCards(getId());

            ps.setInt(36, bookCover);
            ps.setInt(37, vanquisherStage);
            ps.setInt(38, dojoPoints);
            ps.setInt(39, dojoStage);
            ps.setInt(40, finishedDojoTutorial ? 1 : 0);
            ps.setInt(41, vanquisherKills);
            ps.setInt(42, matchcardwins);
            ps.setInt(43, matchcardlosses);
            ps.setInt(44, matchcardties);
            ps.setInt(45, omokwins);
            ps.setInt(46, omoklosses);
            ps.setInt(47, omokties);
            ps.setString(48, dataString);
            ps.setInt(49, id);

            int updateRows = ps.executeUpdate();
            if (updateRows < 1) {
                throw new RuntimeException("Character not in database (" + id + ")");
            }
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    pets[i].saveToDb();
                }
            }
            deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                ps.setInt(2, keybinding.getKey());
                ps.setInt(3, keybinding.getValue().getType());
                ps.setInt(4, keybinding.getValue().getAction());
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, getId());
            for (int i = 0; i < 5; i++) {
                SkillMacro macro = skillMacros[i];
                if (macro != null) {
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

            for (MapleInventory iv : inventory) {
                for (Item item : iv.list()) {
                    itemsWithType.add(new Pair<>(item, iv.getType()));
                }
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id, con);
			
            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.setLong(5, skill.getValue().expiration);
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                if (savedLocations[savedLocationType.ordinal()] != null) {
                    ps.setString(2, savedLocationType.name());
                    ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                    ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)");
            for (int i = 0; i < getTrockSize(); i++) {
                if (trockmaps.get(i) != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, trockmaps.get(i));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)");
            for (int i = 0; i < getVipTrockSize(); i++) {
                if (viptrockmaps.get(i) != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, viptrockmaps.get(i));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies()) {
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setString(3, entry.getGroup());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM area_info WHERE charid = ?");
            ps = con.prepareStatement("INSERT INTO area_info (id, charid, area, info) VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Short, String> area : area_info.entrySet()) {
                ps.setInt(2, area.getKey());
                ps.setString(3, area.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM eventstats WHERE characterid = ?");
            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement psf;
            try (PreparedStatement pse = con.prepareStatement("INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?)")) {
                psf = con.prepareStatement("INSERT INTO medalmaps VALUES (DEFAULT, ?, ?)");
                ps.setInt(1, id);
                for (MapleQuestStatus q : quests.values()) {
                    ps.setInt(2, q.getQuest().getId());
                    ps.setInt(3, q.getStatus().getId());
                    ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                    ps.setInt(5, q.getForfeited());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        for (int mob : q.getProgress().keySet()) {
                            pse.setInt(1, rs.getInt(1));
                            pse.setInt(2, mob);
                            pse.setString(3, q.getProgress(mob));
                            pse.addBatch();
                        }
                        for (int i = 0; i < q.getMedalMaps().size(); i++) {
                            psf.setInt(1, rs.getInt(1));
                            psf.setInt(2, q.getMedalMaps().get(i));
                            psf.addBatch();
                        }
                        pse.executeBatch();
                        psf.executeBatch();
                    }
                }
            }
            psf.close();
            ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?");
            ps.setInt(1, gmLevel);
            ps.setInt(2, client.getAccID());
            ps.executeUpdate();
            ps.close();
			
            con.commit();
			con.setAutoCommit(true);
			
			if (cashshop != null) {
                cashshop.save(con);
            }
            if (storage != null) {
                storage.saveToDB(con);
            }
        } catch (SQLException | RuntimeException t) {
            FilePrinter.printError(FilePrinter.SAVE_CHAR, t, "Error saving " + name + " Level: " + level + " Job: " + job.getId());
            try {
                con.rollback();
            } catch (SQLException se) {
                FilePrinter.printError(FilePrinter.SAVE_CHAR, se, "Error trying to rollback " + name);
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception ignored) {
            }
        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for %s.#k", "Solaxia", reason)));
        this.isbanned = true;
        TimerManager.getInstance().schedule(() -> client.disconnect(false, false), duration);
    }

    public void sendPolice(String text) {
        String message = getName() + " received this - " + text;
        if (Server.getInstance().isGmOnline()) { //Alert and log if a GM is online
            Server.getInstance().broadcastGMMessage(MaplePacketCreator.sendYellowTip(message));
            FilePrinter.printError("autobanwarning.txt", message + "\r\n");
        } else { //Auto DC and log if no GM is online
            client.disconnect(false, false);
            FilePrinter.printError("autobandced.txt", message + "\r\n");
        }
        //Server.getInstance().broadcastGMMessage(0, MaplePacketCreator.serverNotice(1, getName() + " received this - " + text));
        //announce(MaplePacketCreator.sendPolice(text));
        //this.isbanned = true;
        //TimerManager.getInstance().schedule(new Runnable() {
        //    @Override
        //    public void run() {
        //        client.disconnect(false, false);
        //    }
        //}, 6000);
    }

    public void sendKeymap() {
        client.announce(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        // Always send the macro packet to fix a client side bug when switching characters.
        client.announce(MaplePacketCreator.getMacros(skillMacros));
    }

    public void sendNote(String to, String msg, byte fame) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `fame`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, to);
            ps.setString(2, this.getName());
            ps.setString(3, msg);
            ps.setLong(4, System.currentTimeMillis());
            ps.setByte(5, fame);
            ps.executeUpdate();
        }
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.announce(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = x;
    }

    public void setDojoParty(boolean b) {
        this.dojoParty = b;
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public void setDojoStart() {
        this.dojoMap = map;
        int stage = (map.getId() / 100) % 100;
        this.dojoFinish = System.currentTimeMillis() + (stage > 36 ? 15 : stage / 6 + 5) * 60000;
    }

    public void setRates() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        World worldz = Server.getInstance().getWorld(world);
        int hr = cal.get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15) || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21) || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5) || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11)) {
            this.dropRate = worldz.getDropRate(); //Nerfed
            this.mesoRate = worldz.getMesoRate(); //Nerfed
        } else {
            this.dropRate = worldz.getDropRate();
            this.mesoRate = worldz.getMesoRate();
        }
        if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12) || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18) || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14) || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8) || (haveItem(5211045) && hr > 5 && hr < 11) || haveItem(5211048)) {
            if (isBeginnerJob()) {
                this.expRate = 1; //Nerfed
            } else {
                this.expRate = worldz.getExpRate(); //Nerfed
            }
        } else {
            if (isBeginnerJob()) {
                this.expRate = 1;
            } else {
                this.expRate = worldz.getExpRate();
            }
        }
        this.expRate += (client.hasVotedAlready() ? 1 : 0);
        this.mesoRate += getGuild() != null ? 1 : 0;
        this.dropRate += level / 100;
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?")) {
                ps.setInt(1, set ? 1 : 0);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        hasMerchant = set;
    }

    public void addMerchantMesos(int add) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, merchantmeso + add);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            return;
        }
        merchantmeso += add;
    }

    public void setMerchantMeso(int set) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, set);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            return;
        }
        merchantmeso = set;
    }

    public void setHiredMerchant(HiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }

    public void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, hp);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMarkedMonster(int markedMonster) {
        this.markedMonster = markedMonster;
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxHp(int hp, boolean ap) {
        hp = Math.min(30000, hp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp, boolean ap) {
        mp = Math.min(30000, mp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE `characters` SET `name` = ? WHERE `id` = ?");
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParty(MapleParty party) {
        if (party == null) {
            this.mpc = null;
        }
        this.party = party;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp[GameConstants.getSkillBook(job.getId())] = remainingSp; //default
    }

    public void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public byte getSlots(int type) {
        return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        slots += inventory[type].getSlotLimit();
        if (slots <= 96) {
            inventory[type].setSlotLimit(slots);

            saveToDB();
            if (update) {
                client.announce(MaplePacketCreator.updateInventorySlotLimit(type, slots));
            }

            return true;
        }

        return false;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public void showDojoClock() {
        int stage = (map.getId() / 100) % 100;
        long time;
        if (stage % 6 == 1) {
            time = (stage > 36 ? 15 : stage / 6 + 5) * 60;
        } else {
            time = (dojoFinish - System.currentTimeMillis()) / 1000;
        }
        if (stage % 6 > 0) {
            client.announce(MaplePacketCreator.getClock((int) time));
        }
        boolean rightmap = true;
        int clockid = (dojoMap.getId() / 100) % 100;
        if (map.getId() > clockid / 6 * 6 + 6 || map.getId() < clockid / 6 * 6) {
            rightmap = false;
        }
        final boolean rightMap = rightmap; // lol
        TimerManager.getInstance().schedule(() -> {
            if (rightMap) {
                client.getPlayer().changeMap(client.getChannelServer().getMapFactory().getMap(925020000));
            }
        }, time * 1000 + 3000); // let the TIMES UP display for 3 seconds, then warp
    }

    public void showNote() {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM notes WHERE `to`=? AND `deleted` = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ps.setString(1, this.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.last();
                    int count = rs.getRow();
                    rs.first();
                    client.announce(MaplePacketCreator.showNotes(rs, count));
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            Server.getInstance().getWorld(world).updateParty(party.getId(), PartyOperation.SILENT_UPDATE, getMPC());
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(skillId);
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
        ScheduledFuture<?> schedule;
        schedule = TimerManager.getInstance().register(() -> {
            int newFullness = pet.getFullness() - decrease;
            if (newFullness <= 5) {
                pet.setFullness(15);
                pet.saveToDb();
                unequipPet(pet, true);
            } else {
                pet.setFullness(newFullness);
                pet.saveToDb();
                Item petz = getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
                if (petz != null) {
                    forceUpdateItem(petz);
            }
        }
        }, 180000, 18000);
        fullnessSchedule[petSlot] = schedule;

    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        getClient().announce(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(() -> getClient().announce(mapEffect.makeDestroyData()), duration);
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        if (this.getPet(this.getPetIndex(pet)) != null) {
            this.getPet(this.getPetIndex(pet)).setSummoned(false);
            this.getPet(this.getPetIndex(pet)).saveToDb();
        }
        cancelFullnessSchedule(getPetIndex(pet));
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        client.announce(MaplePacketCreator.petStatUpdate(this));
        client.announce(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = Server.getInstance().getWorld(world).getChannel(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.client.announce(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, maxhp));
                    }
                }
            }
        }
    }

    public String getQuestInfo(int quest) {
        MapleQuestStatus qs = getQuest(MapleQuest.getInstance(quest));
        return qs.getInfo();
    }

    public void updateQuestInfo(int quest, String info) {
        MapleQuest q = MapleQuest.getInstance(quest);
        MapleQuestStatus qs = getQuest(q);
        qs.setInfo(info);

        quests.put(q.getId(), qs);

        announce(MaplePacketCreator.updateQuest(qs, false));
        if (qs.getQuest().getInfoNumber() > 0) {
            announce(MaplePacketCreator.updateQuest(qs, true));
        }
        announce(MaplePacketCreator.updateQuestInfo(qs.getQuest().getId(), qs.getNpc()));
    }

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuestID(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
            announce(MaplePacketCreator.updateQuestInfo(quest.getQuest().getId(), quest.getNpc()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            announce(MaplePacketCreator.completeQuest(quest.getQuest().getId(), quest.getCompletionTime()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            announce(MaplePacketCreator.updateQuest(quest, false));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.updateQuest(quest, true));
            }
        }
    }

    public void questTimeLimit(final MapleQuest quest, int time) {
        ScheduledFuture<?> sf = TimerManager.getInstance().schedule(() -> {
            announce(MaplePacketCreator.questExpire(quest.getId()));
            MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
            newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
            updateQuest(newStatus);
        }, time * 60 * 1000);
        announce(MaplePacketCreator.addQuestTimeLimit(quest.getId(), time * 60 * 1000));
        timers.add(sf);
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        announce(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<>(stat, newval)), itemReaction, this));
    }

    public void announce(final byte[] packet) {
        client.announce(packet);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden() || client.getPlayer().isGM()) {
            client.announce(MaplePacketCreator.spawnPlayerMapobject(this));
        }

        if (this.isHidden()) {
            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, 0));
            getMap().broadcastGMMessage(this, MaplePacketCreator.giveForeignBuff(getId(), dsstat), false);
        }
    }

    @Override
    public void setObjectId(int id) {
    }

    @Override
    public String toString() {
        return name;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            client.announce(MaplePacketCreator.enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean containsAreaInfo(int area, String info) {
        Short area_ = (short) area;
        return area_info.containsKey(area_) && area_info.get(area_).contains(info);
    }

    public void updateAreaInfo(int area, String info) {
        area_info.put((short) area, info);
        announce(MaplePacketCreator.updateAreaInfo(area, info));
    }

    public String getAreaInfo(int area) {
        return area_info.get((short) area);
    }

    public Map<Short, String> getAreaInfos() {
        return area_info;
    }

    public void autoban(String reason) {
        this.ban(reason);
        announce(MaplePacketCreator.sendPolice(String.format("You have been blocked by the#b %s Police for HACK reason.#k", "Solaxia")));
        TimerManager.getInstance().schedule(() -> client.disconnect(false, false), 5000);
        Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(6, MapleCharacter.makeMapleReadable(this.name) + " was autobanned for " + reason));
    }

    public void block(int reason, int days, String desc) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")) {
                ps.setString(1, desc);
                ps.setTimestamp(2, TS);
                ps.setInt(3, reason);
                ps.setInt(4, accountid);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    public boolean isBanned() {
        return isbanned;
    }

    public List<Integer> getTrockMaps() {
        return trockmaps;
    }

    public List<Integer> getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = trockmaps.indexOf(999999999);
        if (ret == -1) {
            ret = 5;
        }

        return ret;
    }

    public void deleteFromTrocks(int map) {
        trockmaps.remove(Integer.valueOf(map));
        while (trockmaps.size() < 10) {
            trockmaps.add(999999999);
        }
    }

    public void addTrockMap() {
        int index = trockmaps.indexOf(999999999);
        if (index != -1) {
            trockmaps.set(index, getMapId());
        }
    }

    public boolean isTrockMap(int id) {
        int index = trockmaps.indexOf(id);
        if (index != -1) {
            return true;
        }

        return false;
    }

    public int getVipTrockSize() {
        int ret = viptrockmaps.indexOf(999999999);

        if (ret == -1) {
            ret = 10;
        }

        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        viptrockmaps.remove(Integer.valueOf(map));
        while (viptrockmaps.size() < 10) {
            viptrockmaps.add(999999999);
        }
    }

    public void addVipTrockMap() {
        int index = viptrockmaps.indexOf(999999999);
        if (index != -1) {
            viptrockmaps.set(index, getMapId());
        }
    }

    public boolean isVipTrockMap(int id) {
        int index = viptrockmaps.indexOf(id);
        if (index != -1) {
            return true;
        }

        return false;
    }
    //EVENTS
    private byte team = 0;
    private MapleFitness fitness;
    private MapleOla ola;
    private long snowballattack;

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public MapleOla getOla() {
        return ola;
    }

    public void setOla(MapleOla ola) {
        this.ola = ola;
    }

    public MapleFitness getFitness() {
        return fitness;
    }

    public void setFitness(MapleFitness fit) {
        this.fitness = fit;
    }

    public long getLastSnowballAttack() {
        return snowballattack;
    }

    public void setLastSnowballAttack(long time) {
        this.snowballattack = time;
    }
    //Monster Carnival
    private int cp = 0;
    private int obtainedcp = 0;
    private MonsterCarnivalParty carnivalparty;
    private MonsterCarnival carnival;

    public MonsterCarnivalParty getCarnivalParty() {
        return carnivalparty;
    }

    public void setCarnivalParty(MonsterCarnivalParty party) {
        this.carnivalparty = party;
    }

    public MonsterCarnival getCarnival() {
        return carnival;
    }

    public void setCarnival(MonsterCarnival car) {
        this.carnival = car;
    }

    public int getCP() {
        return cp;
    }

    public int getObtainedCP() {
        return obtainedcp;
    }

    public void addCP(int cp) {
        this.cp += cp;
        this.obtainedcp += cp;
    }

    public void useCP(int cp) {
        this.cp -= cp;
    }

    public void setObtainedCP(int cp) {
        this.obtainedcp = cp;
    }

    public int getAndRemoveCP() {
        int rCP = 10;
        if (cp < 9) {
            rCP = cp;
            cp = 0;
        } else {
            cp -= 10;
        }

        return rCP;
    }

    public AutobanManager getAutobanManager() {
        return autoban;
    }

    public void equipPendantOfSpirit() {
        if (pendantOfSpirit == null) {
            pendantOfSpirit = TimerManager.getInstance().register(() -> {
                if (pendantExp < 3) {
                    pendantExp++;
                    message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
                } else {
                    pendantOfSpirit.cancel(false);
                }
            }, 3600000); //1 hour
        }
    }

    public void unequipPendantOfSpirit() {
        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(false);
            pendantOfSpirit = null;
        }
        pendantExp = 0;
    }

    public void increaseEquipExp(int mobexp) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (Item item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = mii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }

            if ((itemName.contains("Reverse") && nEquip.getItemLevel() < 4) || itemName.contains("Timeless") && nEquip.getItemLevel() < 6) {
                nEquip.gainItemExp(client, mobexp, itemName.contains("Timeless"));
            }
        }
    }

    public Map<String, MapleEvents> getEvents() {
        return events;
    }

    public PartyQuest getPartyQuest() {
        return partyQuest;
    }

    public void setPartyQuest(PartyQuest pq) {
        this.partyQuest = pq;
    }

    public final void empty(final boolean remove) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
        if (beholderHealingSchedule != null) {
            beholderHealingSchedule.cancel(false);
        }
        if (beholderBuffSchedule != null) {
            beholderBuffSchedule.cancel(false);
        }
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        if (recoveryTask != null) {
            recoveryTask.cancel(false);
        }
        cancelExpirationTask();
        for (ScheduledFuture<?> sf : timers) {
            sf.cancel(false);
        }
        timers.clear();
        if (maplemount != null) {
            maplemount.empty();
            maplemount = null;
        }
        if (remove) {
            partyQuest = null;
            events = null;
            mpc = null;
            mgc = null;
            events = null;
            party = null;
            family = null;
            client = null;
            map = null;
            timers = null;
        }
    }

    public void logOff() {
        this.loggedIn = false;
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public void setMapId(int mapid) {
        this.mapid = mapid;
    }

    public boolean getWhiteChat() {
    	return !isGM() ? false : whiteChat;
    }

    public void toggleWhiteChat() {
        whiteChat = !whiteChat;
    }

    public boolean canDropMeso() {
        if (System.currentTimeMillis() - lastMesoDrop >= 200 || lastMesoDrop == -1) { //About 200 meso drops a minute
            lastMesoDrop = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    // These need to be renamed, but I am too lazy right now to go through the scripts and rename them...
    public String getPartyQuestItems() {
        return dataString;
    }

    public boolean gotPartyQuestItem(String partyquestchar) {
        return dataString.contains(partyquestchar);
    }

    public void removePartyQuestItem(String letter) {
        if (gotPartyQuestItem(letter)) {
            dataString = dataString.substring(0, dataString.indexOf(letter)) + dataString.substring(dataString.indexOf(letter) + letter.length());
        }
    }

    public void setPartyQuestItemObtained(String partyquestchar) {
        if (!dataString.contains(partyquestchar)) {
            this.dataString += partyquestchar;
        }
    }

    public void createDragon() {
        dragon = new MapleDragon(this);
    }

    public MapleDragon getDragon() {
        return dragon;
    }

    public void setDragon(MapleDragon dragon) {
        this.dragon = dragon;
    }

    public int getRemainingSpSize() {
        int sp = 0;
        for (int aRemainingSp : remainingSp) {
            if (aRemainingSp > 0) {
                sp++;
            }
        }
        return sp;
    }
}