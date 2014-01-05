#jGalapagos
***
Projekt primjene informacijske tehnologije Ministarstva znanosti, obrazovanja i športa Republike Hrvatske; voditelj projekta: dr.sc. Marko Èupiæ
***
![jGalapagos logo](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/jGalapagos.PNG)

Sustav za izradu rasporeda provjera znanja ostvaren je da ispuni specifiène zahtjeve provjera znanja na Fakultetu elektrotehnike i raèunarstva, ali moguæe ga je primijeniti i na drugim fakultetima. Automatizira raspodjelu provjera znanja na preddefiniranom skupu termina tako da budu ispunjena sljedeca ogranièenja:
* niti jedan student istovremeno ne smije imati više ispita, 
* kapaciteti termina ne smiju biti prekoraèeni i 
* provjere znanja moraju biti održane u jednom od prihvatljivih termina. 

Nakon što su sva ogranièenja ispunjena, nastoji se poboljšati kvaliteta rasporeda tako da teži predmeti s više studenata budu što je moguæe više vremenski udaljeni. Takoðer, nastoji se napraviti što kvalitetniji raspored za redovne studente. Sustav je sposoban izraðivati rasporede samostalnih provjera znanja i rasporede ponovljenih provjera znanja. Ponovljene provjere ispita vremenski slijede nakon samostalnih provjera znanja. Izrada ponovljenih provjera ispita razlikuje se od izrade samostalnih jer se nastoji što je moguæe više vremenski odvojiti provjere znanja istih predmeta. Sustav za izradu rasporeda provjera znanja ostvaren je preko modula platforme jGalapagos - platforme za distribuirano izvršavanje algoritama evolucijskog raèunanja. Omoguæava jednostavno raspodjeljivanje evolucijskog raèunanja na više raèunala, gdje jedno raèunalo predstavlja voditelja, a ostala raèunala radnike. Na radnicima treba biti pokrenut jednostavan program za komunikaciju radnika s voditeljem. Nakon što se na voditelju uèita problem, definiraju se konfiguracije algoritama, parametri migracije i drugi potrebni podaci, a voditelj zapoèinje komunikaciju s radnicima. Šalju se svi potrebni podaci, a na svakom radniku inicijaliziraju se algoritmi. Za vrijeme rada na voditelju se mogu vidjeti trenutaèni statistièki podaci, najbolja rješenja i sl.

Prikaz rješenja nalazi se na slici 2.
![Prikaz rješenja](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/PrikazRjesenja.PNG)
Fenotip predstavlja raspored, a genotip je skup termina.

Primjer paralelizacije prikazan je slikom 3.
![Primjer paralelizacije](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/Parallel.PNG)

Slika 4. prikazuje korištene evolucijske algoritme sustava **jGalapagos**.
![Algoritmi](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/algoritmi.PNG)

Izvorni kod može se pronaæi i na [jGalapagos](http://morgoth.zemris.fer.hr/apiproz/jGalapagos.html).

Sustav jGalapagos aktivno se koristi na Fakultetu elektrotehnike i raèunarstva Sveuèilišta u Zagrebu za centraliziranu izradu rasporeda meðuispita te završnih i ponovljenih ispita na razini cjelokupnog Fakulteta (nekoliko tisuæa studenata te oko 150 kolegija). Koristite li i Vi ovaj paket? Javite nam!

Kontakt: **dr.sc. Marko Èupiæ**, marko.cupic@fer.hr