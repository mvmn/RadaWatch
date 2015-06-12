mysqldump -u root -p radawatch > $(date "+%Y%m%d_%H%M").sql
zip $(date "+%Y%m%d_%H%M").zip $(date "+%Y%m%d_%H%M").sql
rm $(date "+%Y%m%d_%H%M").sql
