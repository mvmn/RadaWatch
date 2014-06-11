**RadaWatch** is a **desktop Java app** (with GUI) that allows one to download information about **votes at Supreme Council of Ukraine and decrees of President of Ukraine** from official websites of those legal bodies. The information is subsequently stored in a relational (SQL) database, and can be queried either directly via SQL queries, or by using in-built analysis tools.

Unfortunately, website of aforementioned legal bodies do not provide any kind of API, thus the information is parsed out from ordinary browser-targeted web pages. This means that parsing may no longer work if something changes on those websites. Thus sooner or later this tool will stop working and will need an update.

It is, however, unlikely that any changes on those websites will occur soon. It's typical for official legal sites in Ukraine to not change for years. And in this case it is a good thing (-:

The embedded RDBMS is a pure-java **H2 DB** ([www.h2database.com](http://www.h2database.com)) that provides it's own web interface, and functionality to backup/restore DB to/from an SQL script.
