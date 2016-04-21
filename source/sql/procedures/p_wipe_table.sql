--
-- UNSAFE - THIS PROCEDURE WIPES A TABLE PERMANENTLY
--

-- Trigger DDL Statements
DELIMITER $$

-- Drop the previously made procedure
DROP PROCEDURE IF EXISTS p_wipe_table $$
-- Create the procedure
CREATE PROCEDURE p_wipe_table (IN p_table VARCHAR(100))
-- Start the procedure
BEGIN
-- Variable to count rows affected
DECLARE rows_affected INT;
-- Prepare the query string
set @query1 = CONCAT('delete from ', p_table, '; ');
set @query2 = CONCAT('ALTER TABLE ', p_table ,' AUTO_INCREMENT=1;');
-- Allow to delete whole tables
SET SQL_SAFE_UPDATES = 0;
-- Prepare executing the actual query
PREPARE stmt1 FROM @query1;
PREPARE stmt2 FROM @query2;
-- Execute the statement
EXECUTE stmt1;
-- Get the rows affected right after delete statement
SET rows_affected = ROW_COUNT();
-- Execute alter table to set AI back to 1
EXECUTE stmt2;
-- Remove the variable
DEALLOCATE PREPARE stmt1;
DEALLOCATE PREPARE stmt2;
-- Set it back to it's previous state
SET SQL_SAFE_UPDATES = 1;

-- Display the amount of rows selected if at least one was affected by the delete query
IF(rows_affected >  0) THEN
    select CONCAT(rows_affected, ' in ', p_table) as 'Rows affected';
END IF;
END $$

DELIMITER ;