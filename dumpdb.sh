FILENAME=RadaWatch_MySQLDB_$(date "+%Y-%m-%d")
mysqldump -u root -p radawatch > $FILENAME.sql
zip $FILENAME.zip $FILENAME.sql
rm $FILENAME.sql
