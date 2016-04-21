--
-- USE WITH EXTREME CAUTION
-- UNSAFE - THIS PROCEDURE WIPES ALL USER DATA
--

-- Trigger DDL Statements
DELIMITER $$
-- Drop the previously made procedure
DROP PROCEDURE IF EXISTS p_wipe $$
-- Create the procedure
CREATE PROCEDURE p_wipe ()
-- Start the procedure
BEGIN

-- Ignore constraints
SET FOREIGN_KEY_CHECKS=0;
-- Start wiping all tables
call p_wipe_table('accounts');
call p_wipe_table('alliance');
call p_wipe_table('area_info');
call p_wipe_table('bbs_replies');
call p_wipe_table('bbs_threads');
-- START _bit - BIT-related (website/forum)
call p_wipe_table('bit_bcomments');
call p_wipe_table('bit_buynx');
call p_wipe_table('bit_ecomments');
call p_wipe_table('bit_events');
call p_wipe_table('bit_gdcache');
call p_wipe_table('bit_gmblog');
call p_wipe_table('bit_mail');
call p_wipe_table('bit_ncomments');
call p_wipe_table('bit_news');
call p_wipe_table('bit_pages');
call p_wipe_table('bit_profile');
call p_wipe_table('bit_tcomments');
call p_wipe_table('bit_tickets');
call p_wipe_table('bit_vote');
call p_wipe_table('bit_votingrecords');
-- END _bit
call p_wipe_table('buddies');
call p_wipe_table('characters');
call p_wipe_table('cooldowns');
call p_wipe_table('dueyitems');
call p_wipe_table('dueypackages');
call p_wipe_table('eventstats');
call p_wipe_table('famelog');
call p_wipe_table('family_character');
call p_wipe_table('gifts');
call p_wipe_table('gmlog');
call p_wipe_table('guilds');
call p_wipe_table('hiredmerchant');
call p_wipe_table('htsquads');
call p_wipe_table('hwidbans');
call p_wipe_table('inventoryequipment');
call p_wipe_table('inventoryitems');
call p_wipe_table('ipbans');
call p_wipe_table('iplog');
call p_wipe_table('keymap');
call p_wipe_table('macbans');
call p_wipe_table('macfilters');
call p_wipe_table('marriages');
call p_wipe_table('medalmaps');
call p_wipe_table('mts_cart');
call p_wipe_table('mts_items');
call p_wipe_table('notes');
call p_wipe_table('nxcode');
call p_wipe_table('pets');
call p_wipe_table('playernpcs');
call p_wipe_table('playernpcs_equip');
call p_wipe_table('questactions');
call p_wipe_table('questprogress');
call p_wipe_table('questrequirements');
call p_wipe_table('queststatus');
call p_wipe_table('reports');
call p_wipe_table('responses');
call p_wipe_table('rings');
call p_wipe_table('savedlocations');
call p_wipe_table('server_queue');
call p_wipe_table('skillmacros');
call p_wipe_table('skills');
call p_wipe_table('storages');
call p_wipe_table('trocklocations');
call p_wipe_table('wishlists');
call p_wipe_table('zaksquads');
-- Re-enable constraints
SET FOREIGN_KEY_CHECKS=1;

-- End the procedure
END $$

DELIMITER ;