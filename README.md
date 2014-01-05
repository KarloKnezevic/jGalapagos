#jGalapagos
***
Projekt primjene informacijske tehnologije Ministarstva znanosti, obrazovanja i �porta Republike Hrvatske; voditelj projekta: dr.sc. Marko �upi�
***
![jGalapagos logo](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/jGalapagos.PNG)

Sustav za izradu rasporeda provjera znanja ostvaren je da ispuni specifi�ne zahtjeve provjera znanja na Fakultetu elektrotehnike i ra�unarstva, ali mogu�e ga je primijeniti i na drugim fakultetima. Automatizira raspodjelu provjera znanja na preddefiniranom skupu termina tako da budu ispunjena sljedeca ograni�enja:
* niti jedan student istovremeno ne smije imati vi�e ispita, 
* kapaciteti termina ne smiju biti prekora�eni i 
* provjere znanja moraju biti odr�ane u jednom od prihvatljivih termina. 

Nakon �to su sva ograni�enja ispunjena, nastoji se pobolj�ati kvaliteta rasporeda tako da te�i predmeti s vi�e studenata budu �to je mogu�e vi�e vremenski udaljeni. Tako�er, nastoji se napraviti �to kvalitetniji raspored za redovne studente. Sustav je sposoban izra�ivati rasporede samostalnih provjera znanja i rasporede ponovljenih provjera znanja. Ponovljene provjere ispita vremenski slijede nakon samostalnih provjera znanja. Izrada ponovljenih provjera ispita razlikuje se od izrade samostalnih jer se nastoji �to je mogu�e vi�e vremenski odvojiti provjere znanja istih predmeta. Sustav za izradu rasporeda provjera znanja ostvaren je preko modula platforme jGalapagos - platforme za distribuirano izvr�avanje algoritama evolucijskog ra�unanja. Omogu�ava jednostavno raspodjeljivanje evolucijskog ra�unanja na vi�e ra�unala, gdje jedno ra�unalo predstavlja voditelja, a ostala ra�unala radnike. Na radnicima treba biti pokrenut jednostavan program za komunikaciju radnika s voditeljem. Nakon �to se na voditelju u�ita problem, definiraju se konfiguracije algoritama, parametri migracije i drugi potrebni podaci, a voditelj zapo�inje komunikaciju s radnicima. �alju se svi potrebni podaci, a na svakom radniku inicijaliziraju se algoritmi. Za vrijeme rada na voditelju se mogu vidjeti trenuta�ni statisti�ki podaci, najbolja rje�enja i sl.

Prikaz rje�enja nalazi se na slici 2.
![Prikaz rje�enja](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/PrikazRjesenja.PNG)
Fenotip predstavlja raspored, a genotip je skup termina.

Primjer paralelizacije prikazan je slikom 3.
![Primjer paralelizacije](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/Parallel.PNG)

Slika 4. prikazuje kori�tene evolucijske algoritme sustava **jGalapagos**.
![Algoritmi](https://raw.github.com/KarloKnezevic/jGalapagos/master/res_gh/algoritmi.PNG)

Izvorni kod mo�e se prona�i i na [jGalapagos](http://morgoth.zemris.fer.hr/apiproz/jGalapagos.html).

Sustav jGalapagos aktivno se koristi na Fakultetu elektrotehnike i ra�unarstva Sveu�ili�ta u Zagrebu za centraliziranu izradu rasporeda me�uispita te zavr�nih i ponovljenih ispita na razini cjelokupnog Fakulteta (nekoliko tisu�a studenata te oko 150 kolegija). Koristite li i Vi ovaj paket? Javite nam!

Kontakt: **dr.sc. Marko �upi�**, marko.cupic@fer.hr