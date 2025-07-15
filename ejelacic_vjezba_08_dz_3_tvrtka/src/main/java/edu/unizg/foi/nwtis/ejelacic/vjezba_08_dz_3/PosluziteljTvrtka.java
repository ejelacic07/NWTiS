package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Kuhinja;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;

/**
 * The Class PosluziteljTvrtka.
 */
public class PosluziteljTvrtka {

  /** Konfiguracijski podaci. */
  Konfiguracija konfig;

  /** Pokretač dretvi. */
  private ExecutorService executor = null;

  /** Pauza dretve. */
  private int pauzaDretve;

  /** Kod za kraj rada. */
  String kodZaKraj = "";

  /** Zastavica za kraj rada. */
  AtomicBoolean kraj = new AtomicBoolean(false);

  String datotekaPartnera = "";

  /** naziv datoteke u koju se spremaju obračuni */
  private String datotekaObracuna;

  /**
   * Getter za dohvaćanje konfiguracije
   * 
   * @return - objekt tipa Konfiguracija
   */

  public Konfiguracija getKonfig() {
    return konfig;
  }

  /** mrezna vrata registracija. */
  private int mreznaVrataRegistracija;

  /** mrezna vrata rad. */
  private int mreznaVrataRad;

  /** Kolekcija kuhinje. */
  Map<String, Kuhinja> kuhinje = new ConcurrentHashMap<>();

  /** Kolekcija jelovnici. */
  Map<String, Jelovnik> jelovnici = new ConcurrentHashMap<>();

  /** Kolekcija kartaPica. */
  Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Lista partneri. */
  List<Partner> partneri = new ArrayList<>();

  private boolean registracijaAktivna = true;

  // static Logger log = Logger.getLogger(PosluziteljTvrtka.class.getName());

  /**
   * Enum Tip - definira dvije konstante REG za registraciju i RAD za rad s partnerima
   */
  enum Tip {

    REG("REG", "registraciju"),

    RAD("RAD", "rad s partnerima");

    private String print;

    /**
     * Instancira novi tip
     *
     * @param tip the tip
     * @param print the print
     */
    Tip(String tip, String print) {
      this.print = print;
    }

    /**
     * Ispis vrijednosti.
     *
     * @return - ispis vrijednosti
     */
    String ispisVrijednosti() {
      return print;
    }

  }

  private static Map<Integer, Boolean> statusi = new HashMap<>();

  static {
    statusi.put(1, true);
    statusi.put(2, true);
  }

  /**
   * Glavna metoda
   *
   * @param args - pokreće metodu za učitavanje postavki i za pokretanje virtualnih dretvi
   * @throws NeispravnaKonfiguracija
   */
  public static void main(String[] args) throws NeispravnaKonfiguracija {

    if (args.length != 1) {
      return;
    }
    var program = new PosluziteljTvrtka();
    var nazivDatoteke = args[0];
    program.pripremiKreni(nazivDatoteke);
    program.pokreniDretve();

  }

  /**
   * pripremiKreni
   */
  public void pripremiKreni(String nazivDatoteke) throws NeispravnaKonfiguracija {

    ucitajKonfiguraciju(nazivDatoteke);
    ucitajPartnere();
    popisKuhinja();
    ucitajJelovnike();

    ucitajKartuPica(this.konfig.dajPostavku("datotekaKartaPica"));

    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavkuOsnovno("pauzaDretve", "1000"));
    this.mreznaVrataRegistracija =
        Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
    this.mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));

  }

  /**
   * PokreniDretve
   */
  public void pokreniDretve() {

    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    Future<?> dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());

    Future<?> dretvaZaRad = executor.submit(() -> pokreniPosluzitelje(mreznaVrataRad, Tip.RAD));

    Future<?> dretvaZaRegistraciju =
        executor.submit(() -> pokreniPosluzitelje(mreznaVrataRegistracija, Tip.REG));

    try {

      while (!this.kraj.get()) {
        Thread.sleep(this.pauzaDretve);
      }

      dretvaZaKraj.cancel(true);
      dretvaZaRad.cancel(true);
      dretvaZaRegistraciju.cancel(true);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      executor.shutdownNow();

    }
  }

  /**
   * Pokreni posluzitelje
   */
  public void pokreniPosluzitelje(int mreznaVrata, Tip tip) {

    try (ServerSocket uticnicaServer = new ServerSocket(mreznaVrata)) {
      System.out.println(
          "Poslužitelj za " + tip.ispisVrijednosti() + " pokrenut na portu: " + mreznaVrata);
      while (!kraj.get()) {
        Socket uticnica = uticnicaServer.accept();
        switch (tip) {
          case REG:
            Thread.startVirtualThread(() -> obradiZahtjev(uticnica));
            break;
          case RAD:
            Thread.startVirtualThread(() -> obradiKlijenta(uticnica));
        }
      }
    } catch (IOException e) {
      System.err
          .println("Greška u poslužitelju za " + tip.ispisVrijednosti() + ": " + e.getMessage());
    }
  }

  /**
   * Obradi klijenta
   */
  public void obradiKlijenta(Socket mreznaUticnica) {
    System.out.println("Ušao sam u metodu obradiKlijenta");
    try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream()));
        PrintWriter pisac = new PrintWriter(mreznaUticnica.getOutputStream(), true)) {
      String linija = in.readLine();
      if (linija == null || linija.isBlank()) {

        pisac.println("ERROR 30:Format komande nije ispravan.");
        return;
      }
      System.out.println(linija);

      String[] dijeloviKomande = linija.trim().split(" ", 3);
      String komanda = dijeloviKomande[0];


      switch (komanda) {
        case "JELOVNIK" -> {

          obradiJelovnik(dijeloviKomande, pisac);
        }
        case "KARTAPIĆA" -> {

          obradiKartuPica(dijeloviKomande, pisac);
        }
        case "OBRAČUN" -> {

          obradiObracun(dijeloviKomande, in, pisac, true);
        }
        case "OBRAČUNWS" -> {

          obradiObracun(dijeloviKomande, in, pisac, false);
        }
        default -> {

          pisac.println("ERROR 30: Format komande nije ispravan");
        }
      }

    } catch (Exception e) {

      System.err.println("Greška klijenta: " + e.getMessage());
    }
  }

  /**
   * pokreniPosluziteljKraj
   */
  public void pokreniPosluziteljKraj() {

    var mreznaVrataKraj = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

    var brojCekaca = 0;


    try (ServerSocket ss = new ServerSocket(mreznaVrataKraj, brojCekaca)) {


      while (!this.kraj.get()) {

        var mreznaUticnica = ss.accept();

        if (this.obradiKrajOtvaranje(mreznaUticnica)) {

          ss.close();

          this.kraj.set(true);

        } else {

          ss.close();

        }
      }
    } catch (IOException e) {
    }
  }

  /**
   * ucitajJelovnike
   */
  public void ucitajJelovnike() {

    jelovnici = new ConcurrentHashMap<>();

    for (String oznaka : kuhinje.keySet()) {

      String nazivDatoteke = oznaka + ".json";


      Path datoteka = Path.of(nazivDatoteke);


      if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka)
          || !Files.isReadable(datoteka)) {

        Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
            "Datoteka " + nazivDatoteke + " nije pronađena");
        continue;
      }

      try (var br = Files.newBufferedReader(datoteka)) {

        Gson gson = new Gson();


        var karticaJelovnika = gson.fromJson(br, Jelovnik[].class);


        var karticaJelovnikaTok = Stream.of(karticaJelovnika);


        karticaJelovnikaTok.forEach(kp -> {
          this.jelovnici.put(kp.id(), kp);

        });
      } catch (IOException ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
            "Greška kod upisa datoteke, preskačem " + nazivDatoteke);
      }
    }
  }

  /**
   * UcitajPartnere
   */
  public void ucitajPartnere() {

    String nazivDatoteke = konfig.dajPostavku("datotekaPartnera");


    if (nazivDatoteke == null || nazivDatoteke.isEmpty()) {

      Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
          "Datoteka partnera je prazna ili ne postoji u konfiguraciji");
      return;
    }

    var datoteka = Path.of(nazivDatoteke);


    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {

      Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
          "Datoteka partnera " + datoteka + " nije pronađena");
      return;
    }

    try (var bufferedReader = Files.newBufferedReader(datoteka)) {

      Gson gson = new Gson();


      var partnerNiz = gson.fromJson(bufferedReader, Partner[].class);

      var partnerTok = Stream.of(partnerNiz);

      partnerTok.forEach(par -> {
        this.partneri.add(par);

      });
    } catch (IOException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
          "Datoteka partnera " + datoteka + " nije pronađena");
    }
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var kartaPicaNiz = gson.fromJson(br, KartaPica[].class);
      var kartaPicaTok = Stream.of(kartaPicaNiz);
      kartaPicaTok.forEach(kp -> {
        this.kartaPica.put(kp.id(), kp);

      });
    } catch (IOException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
          "Greška kod spremanja karte pića: " + ex.getMessage());
    }
  }

  /**
   * obradiKraj
   */
  public Boolean obradiKrajOtvaranje(Socket mreznaUticnica) {

    PrintWriter pisac = null;
    boolean kraj = false;


    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));


      pisac = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));


      String linija = in.readLine();


      mreznaUticnica.shutdownInput();


      String[] dijelovi = linija.trim().split("\\s+");


      String naredba = dijelovi[0].toUpperCase();


      switch (naredba) {
        case "STATUS" -> {

          obradiStatus(dijelovi, in, pisac);
        }
        case "PAUZA" -> {

          obradiPauza(dijelovi, in, pisac);
        }
        case "START" -> {

          obradiStart(dijelovi, in, pisac);
        }
        case "SPAVA" -> {

          obradiSpava(dijelovi, in, pisac);
        }
        case "KRAJWS" -> {

          kraj = obradiKrajWS(dijelovi, in, pisac);
        }
        case "OSVJEŽI" -> {

          obradiOsvjezi(dijelovi, in, pisac);
        }
        case "KRAJ" -> {

          kraj = obradiKraj(dijelovi, in, pisac, mreznaUticnica);
        }
        default -> {

          pisac.write("ERROR 19: Nešto drugo nije u redu.\n");
        }
      }
      pisac.flush();

    } catch (Exception e) {

      if (pisac != null) {
        pisac.write("ERROR 19: Nešto drugo nije u redu.\n");
        pisac.flush();
      }
    }
    return kraj;
  }

  private boolean obradiKraj(String[] dijelovi, BufferedReader in, PrintWriter pisac,
      Socket mreznaUticnica) {

    String kod = dijelovi[1];


    if (!(kod.equals(kodZaKraj) || kod.equals("XXX"))) {

      pisac.write("ERROR 10: Format komande ili kod za kraj nije ispravan\n");
      pisac.flush();
      return false;
    }

    if (kod.equals("XXX")) {

      if (!this.kraj.get()) {

        pisac.write("OK\n");
        pisac.flush();
        return true;
      } else {

        pisac.write("ERROR 13: Pogrešna promjena pauze ili starta.\n");
        pisac.flush();
        return false;
      }
    }
    try {

      pisac.write("OK\n");
      pisac.flush();
      mreznaUticnica.shutdownOutput();

      mreznaUticnica.close();

      return true;
    } catch (IOException e) {

      pisac.write("ERROR 19: Nešto drugo nije u redu.\n");
      pisac.flush();
      return false;
    }
  }

  private void obradiStatus(String[] dijeloviKomande, BufferedReader in, PrintWriter pisac) {

    for (int i = 0; i < dijeloviKomande.length; i++) {

    }

    if (!provjerDijelove(dijeloviKomande, 3)) {

      pisac.write("ERROR 10: Format komande nije ispravan ili nije ispravan kod za kraj");
      return;
    }

    String kod = dijeloviKomande[1];


    Integer dio = Integer.valueOf(dijeloviKomande[2]);


    String ADMIN_KOD = getKonfig().dajPostavku("kodZaAdminTvrtke");


    System.out.println("Kod partnera " + kod);
    if (!ADMIN_KOD.equals(kod)) {

      pisac.write("ERROR 12: Pogrešan kodZaAdminTvrtke");
      return;
    }

    if (!statusi.containsKey(dio)) {

      pisac.write("ERROR 12: Pogrešan kodZaAdminTvrtke");
      return;
    }

    boolean status = statusi.get(dio);

    pisac.write("OK " + (status ? "1" : "0"));
  }

  private void obradiPauza(String[] dijeloviKomande, BufferedReader in, PrintWriter pisac) {

    if (!provjerDijelove(dijeloviKomande, 2)) {

      pisac.write("ERROR 10: Format komande nije ispravan ili nije ispravan kod za kraj");
      return;
    }

    String kod = dijeloviKomande[1];


    Integer dio = Integer.valueOf(dijeloviKomande[2]);


    String ADMIN_KOD = getKonfig().dajPostavku("kodZaAdminTvrtke");


    if (!ADMIN_KOD.equals(kod)) {

      pisac.write("ERROR 12: Pogrešan kodZaAdminTvrtke");
      return;
    }

    if (!statusi.containsKey(dio)) {

      pisac.write("ERROR 13: Pogrešna promjena pauze ili starta");
      return;
    }

    if (statusi.get(dio)) {

      statusi.put(dio, false);
    }
    pisac.write("OK");
  }

  private void obradiStart(String[] dijeloviKomande, BufferedReader in, PrintWriter pisac) {

    if (!provjerDijelove(dijeloviKomande, 2)) {

      pisac.write("ERROR 10: Format komande nije ispravan ili nije ispravan kod za kraj");
      return;
    }

    String ADMIN_KOD = getKonfig().dajPostavku("kodZaAdminTvrtke");


    String kod = dijeloviKomande[1];


    Integer dio = Integer.valueOf(dijeloviKomande[2]);


    if (!ADMIN_KOD.equals(kod)) {

      pisac.write("ERROR 12: Pogrešan kodZaAdminTvrtke");
      return;
    }

    if (!statusi.containsKey(dio)) {

      pisac.write("ERROR 13: Pogrešna promjena pauze ili starta");
      return;
    }

    if (!statusi.get(dio)) {

      statusi.put(dio, true);
    }
    pisac.write("OK");
  }

  private void obradiSpava(String[] dijeloviKomande, BufferedReader in, PrintWriter pisac) {

    if (!provjerDijelove(dijeloviKomande, 2)) {

      pisac.write("ERROR 10: Format komande nije ispravan ili nije ispravan kod za kraj");
      return;
    }

    String kod = dijeloviKomande[1];


    Integer ms = Integer.valueOf(dijeloviKomande[2]);


    String ADMIN_KOD = getKonfig().dajPostavku("kodZaAdminTvrtke");


    if (!ADMIN_KOD.equals(kod)) {

      pisac.write("ERROR 12: Pogrešan kodZaAdminTvrtke");
      return;
    }

    try {

      Thread.sleep(ms);

    } catch (InterruptedException e) {
      pisac.write("ERROR 16: Prekid spavanja dretve");
      return;
    }
    pisac.write("OK");
  }

  private boolean obradiKrajWS(String[] dijeloviKomande, BufferedReader in, PrintWriter pisac) {

    if (!provjerDijelove(dijeloviKomande, 1)) {

      pisac.write("ERROR 10: Format komande nije ispravan ili nije ispravan kod za kraj");
      return false;
    }

    String kod = dijeloviKomande[1];


    String KRAJ_KOD = getKonfig().dajPostavku("kodZaKraj");


    if (!KRAJ_KOD.equals(kod)) {

      pisac.write("ERROR 12: Pogrešan kodZaKraj");
      return false;
    }

    boolean sviPartneriZavrsili = true;


    if (sviPartneriZavrsili) {

      pisac.write("OK");
      return true;
    } else {

      pisac.write("ERROR 14: Barem jedan partner nije završio rad");
      return false;
    }
  }

  private void obradiOsvjezi(String[] dijeloviKomande, BufferedReader in, PrintWriter pisac) {

    if (!provjerDijelove(dijeloviKomande, 1)) {
      pisac.write("ERROR 10: Format komande nije ispravan ili nije ispravan kod za kraj");
      return;
    }
    String ADMIN_KOD = getKonfig().dajPostavku("kodZaAdminTvrtke");
    String kod = dijeloviKomande[1];
    if (!ADMIN_KOD.equals(kod)) {
      pisac.write("ERROR 12: Pogrešan kodZaAdminTvrtke");
      return;
    }
    if (statusi.get(2)) {
      ucitajJelovnike();
      ucitajKartuPica(konfig.dajPostavku("datotekaKartaPica"));
      pisac.write("OK");
    } else {
      pisac.write("ERROR 15: Poslužitelj za partnere u pauzi");
    }
  }

  private static boolean provjerDijelove(String[] dijelovi, int brArg) {
    if (dijelovi.length != brArg)
      return false;
    return true;
  }

  /**
   * obradiJelovnik
   * 
   * Ako sigurnosni kod pripada registriranom partneru, učitava podatke o jelovnicima u kolekciju
   * jelovnici
   *
   * @param dijeloviKomande - niz s dijelovima komande
   * @param out - PrintWriter za odgvovor (sigKod partnera)
   */
  public void obradiJelovnik(String[] dijeloviKomande, PrintWriter out) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    if (dijeloviKomande.length < 3) {
      out.println("ERROR 30: Format komande nije ispravan");
      return;
    }

    try {
      int id = Integer.parseInt(dijeloviKomande[1]);
      String kod = dijeloviKomande[2];

      Partner partner = partneri.stream().filter(par -> par.id() == id).findFirst().orElse(null);
      if (partner == null || !partner.sigurnosniKod().equals(kod)) {

        out.println("ERROR 31: Ne postoji partner s ID-jem " + id + " u kolekciji");
        return;
      }
      List<Jelovnik> jelovnik = new ArrayList<>();

      for (String kljuc : jelovnici.keySet()) {
        if (kljuc.contains(partner.vrstaKuhinje())) {
          jelovnik.add(jelovnici.get(kljuc));
        }
      }

      if (jelovnik.size() == 0) {
        out.println("ERROR 32: Ne postoji jelovnik s tom vrstom kuhinje");
        return;
      }

      out.print("OK " + gson.toJson(jelovnik));

    } catch (Exception e) {
      out.println("ERROR 39: Nepoznata pogreška");
    }
  }

  /**
   * obradiKartuPica
   * 
   * Ako sigurnosni kod pripada registriranom partneru, učitava podatke o pićima u kolekciju
   * kartaPica
   *
   * @param dijeloviKomande - niz s dijelovima komande
   * @param out - PrintWriter za odgvovor (sigKod partnera)
   */
  public void obradiKartuPica(String[] dijeloviKomande, PrintWriter out) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    if (dijeloviKomande.length < 3) {
      out.println("ERROR 30:Format komande nije ispravan");
      return;
    }

    try {
      int id = Integer.parseInt(dijeloviKomande[1]);
      String kod = dijeloviKomande[2];

      Partner partner = partneri.stream().filter(par -> par.id() == id).findFirst().orElse(null);
      if (partner == null || !partner.sigurnosniKod().equals(kod)) {
        out.println("ERROR 31: Ne postoji partner s tim ID-jem ili sigurnosni kod nije ispravan");

        return;
      }


      List<KartaPica> kartaPica = new ArrayList<>();

      for (String kljuc : this.kartaPica.keySet()) {
        kartaPica.add(this.kartaPica.get(kljuc));
      }

      out.println("OK " + gson.toJson(kartaPica));

    } catch (Exception e) {
      out.println("ERROR 39: Nepoznata pogreška");
    }
  }

  /**
   * obradiObracun
   * 
   * Ako pridruženi partner postoji u kolekciji partneri, dodaje obračun na listu Obracun i u
   * kolekciju sviObracuni. Vraća "OK" ili ispis pogreške.
   *
   * @param dijeloviKomande - niz s dijelovima komande
   * @param out - PrintWriter za odgovor (sigKod partnera)
   * @param in - BufferedReader koji čita dobivene podatke
   */
  public void obradiObracun(String[] dijeloviKomande, BufferedReader in, PrintWriter out,
      boolean saljiRest) {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    if (dijeloviKomande.length < 2) {
      out.println("ERROR 30: Format komande nije ispravan");
      return;
    }
    try {
      int id = Integer.parseInt(dijeloviKomande[1]);
      String kod = dijeloviKomande[2];
      Partner partner = partneri.stream().filter(par -> par.id() == id).findFirst().orElse(null);
      if (partner == null || !partner.sigurnosniKod().equals(kod)) {
        out.println("ERROR 31: Ne postoji partner s tim ID-jem");
        return;
      }
      if (registracijaAktivna) {
        out.println("ERROR 36: Poslužitelj za partnere u pauzi");
        return;
      }
      StringBuilder jsonBuilder = new StringBuilder();
      String linija;
      while ((linija = in.readLine()) != null) {
        jsonBuilder.append(linija).append("\n");
        if (linija.contains("]"))
          break;
      }
      String json = jsonBuilder.toString().trim();
      List<Obracun> obracuniNovi = gson.fromJson(json, new TypeToken<List<Obracun>>() {}.getType());
      List<Obracun> sviObracuni = ucitajIzJSON(konfig.dajPostavku("datotekaObracuna"));

      sviObracuni.addAll(obracuniNovi);

      if (!spremiPodatkeObracun(sviObracuni, konfig.dajPostavku("datotekaObracuna"), saljiRest,
          obracuniNovi)) {
        out.println("ERROR 36: Poslužitelj za partnere u pauzi");
        return;
      }
      // ERROR 37: RESTful zhatjev nije uspješan
      out.println("OK");
    } catch (Exception e) {
      out.println("ERROR 35: Neispravan obračun");
    }
  }

  /**
   * ucitajIzJSON
   * 
   * 
   */

  public List<Obracun> ucitajIzJSON(String nazivDatoteke) throws IOException {
    Path datoteka = Paths.get(nazivDatoteke);

    if (!Files.exists(datoteka)) {
      try {
        Files.createFile(datoteka);
        try (FileWriter writer = new FileWriter(datoteka.toFile())) {
          writer.write("[]");
        }
      } catch (IOException e) {
        System.out.println("Greška pri kreiranju datoteke: " + e.getMessage());
        return null;
      }
    }
    try (FileReader reader = new FileReader(nazivDatoteke)) {
      Gson gson = new Gson();
      return gson.fromJson(reader, new TypeToken<List<Obracun>>() {}.getType());
    }
  }

  /**
   * obradiZahtjev
   * 
   * Ako PosluziteljPartner šalje komandu, pokreće metodu ovisno o nazivu komande. Vraća ispis
   * pogreške s problemom u slučaju da komanda nije ispravna.
   *
   * 
   * @param mreznaUticnica - mrezna utičnica za povezivanje klijenta posluziteljPartner
   */

  public void obradiZahtjev(Socket mreznaUticnica) {
    // provjera ako je u pauzi i ispis greške
    try (
        BufferedReader citac =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream()));
        PrintWriter pisac = new PrintWriter(mreznaUticnica.getOutputStream(), true)) {
      String zahtjev = citac.readLine();
      if (zahtjev == null || zahtjev.isBlank()) {
        pisac.println("ERROR 20: Format komande nije ispravan");
        return;
      } // PARTNER 1 "FOLT 1 - Varaždin, Pavlinska 9" MK localhost 8001 46.30803 16.34009 8000
        // FE98DC76BA54
      if (zahtjev.startsWith("PARTNER")) {
        obradiPartnerKomandu(zahtjev, pisac);
      } else if (zahtjev.startsWith("OBRIŠI")) {
        obradiObrisiKomandu(zahtjev, pisac);
      } else if (zahtjev.equals("POPIS")) {
        obradiPopisKomandu(pisac);
      } else {
        pisac.println("ERROR 20: Format komande nije ispravan");
      }
    } catch (IOException e) {
      System.err.println("ERROR 29: Nešto drugo nije u redu. " + e.getMessage());
    }
  }

  /**
   * obradiPopisKomandu
   *
   * Dodaje partera u zapis ParterPopis. U slučaju pogreške vraća ispis pogreške s problemom.
   *
   *
   * @param pisac -PrintWriter za ispis odgovora
   */
  public void obradiPopisKomandu(PrintWriter pisac) {
    try {
      List<PartnerPopis> popis;
      synchronized (partneri) {
        popis = partneri.stream().map(PartnerPopis::new).toList();
      }
      String json = toJson(popis);
      pisac.println("OK");
      pisac.println(json);
    } catch (Exception e) {
      pisac.println("ERROR 29: Neočekivana greška kod zapisa JSONa");
    }
  }

  public String toJson(List<PartnerPopis> popis) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(popis);
  }

  /**
   * obradiObrisiKomanu
   * 
   * Provjerava ako je partner registriran te ako nije, vraća kod pogreške. Ako postoji registriran
   * partner briše sve podatke o partnerima iz liste partneri i vraća "OK"
   *
   * @param zahtjev - komanda s poslužitelja
   * @param pisac - PrintWriter za ispis odgovora
   */
  public void obradiObrisiKomandu(String zahtjev, PrintWriter pisac) {
    try {
      String[] dijelovi = zahtjev.split(" ");
      if (dijelovi.length != 3) {
        pisac.println("ERROR 20: Format komande nije ispravan");
        return;
      }
      int id = Integer.parseInt(dijelovi[1]);
      String kod = dijelovi[2];
      synchronized (partneri) {
        Optional<Partner> zaBrisanje = partneri.stream().filter(p -> p.id() == id).findFirst();
        if (zaBrisanje.isEmpty()) {
          pisac.println("ERROR 23: Ne postoji partner u kolekciji partnera");
          return;
        }
        Partner p = zaBrisanje.get();
        if (!p.sigurnosniKod().equals(kod)) {
          pisac.println("ERROR 22: Neispravan sigurnosi kod partnera");
          return;
        }
        partneri.remove(p);
        pisac.println("OK");
      }
    } catch (Exception e) {
      pisac.println("ERROR 29:Nepoznata pogreška");
    }
  }

  /**
   * obradiPartnerKomandu
   * 
   * Dodaje partnere na listu partneri, provjerava ako je partner već registriran. Stvara sigurnosni
   * kod za partnera, sprema podatke o njemu i vraća "OK sigKod".
   *
   * @param zahtjev - komanda klijenta posluziteljPartner
   * @param pisac - PrintWriter za ispis odgovora.
   */

  public void obradiPartnerKomandu(String zahtjev, PrintWriter pisac) {
    try {
      if (!registracijaAktivna()) {
        pisac.write("ERROR 24: Poslužitelj za registraciju partnera u pauzi");
        return;
      }
      String[] dijelovi = new String[10];
      Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(zahtjev);
      int i = 0;

      while (m.find()) {
        if (m.group(1) != null) {
          dijelovi[i] = m.group(1);
        } else {
          dijelovi[i] = m.group(2);
        }

        i++;
      }


      if ((i != 11) && (i != 10)) {
        pisac.println("ERROR 20: Format komande nije ispravan");
        return;
      }
      int id = Integer.parseInt(dijelovi[1]);
      String naziv = dijelovi[2].replace("\"", "");
      String vrstaKuhinje = dijelovi[3];
      String adresa = dijelovi[4];
      int mreznaVrata = Integer.parseInt(dijelovi[5]);
      float lat = Float.parseFloat(dijelovi[6]);
      float lon = Float.parseFloat(dijelovi[7]);
      int mreznaVrataKraj = Integer.parseInt(dijelovi[8]);
      String adminKod = dijelovi[9];
      synchronized (partneri) {
        if (partneri.stream().anyMatch(p -> p.id() == id)) {
          pisac.println("ERROR 21: Već postoji partner s id u kolekciji partnera");
          return;
        }
        String hashInput = naziv + adresa;
        String sigurnosniKod = Integer.toHexString(hashInput.hashCode());
        Partner noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata,
            mreznaVrataKraj, lat, lon, sigurnosniKod, adminKod);
        partneri.add(noviPartner);
        spremiPodatkePartner(partneri, konfig.dajPostavku("datotekaPartnera"));
        pisac.println("OK " + sigurnosniKod);
      }
    } catch (Exception e) {
      pisac.println("ERROR 29: Nepoznata pogreška");
    }
  }

  private boolean registracijaAktivna() {
    return registracijaAktivna;
  }

  /**
   * spremiPodatkePartner
   * 
   * 
   */
  public void spremiPodatkePartner(List<Partner> partneri, String nazivDatoteke) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(nazivDatoteke)) {
      gson.toJson(partneri, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * spremiPodatkeObracun
   * 
   * 
   */

  public boolean spremiPodatkeObracun(List<Obracun> lista, String nazivDatoteke, boolean saljiRest,
      List<Obracun> obracunNovi) throws IOException {
    try (FileWriter writer = new FileWriter(nazivDatoteke)) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(lista, writer);
    }
    if (saljiRest) {
      return pripremiRestPoziv(obracunNovi);
    }
    return true;
  }


  private boolean pripremiRestPoziv(List<Obracun> obracunNovi) {
    try {
      URL url = new URL(konfig.dajPostavku("restAdresa") + "/obracun");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/json");

      Gson gson = new Gson();
      String jsonPayload = gson.toJson(obracunNovi);

      try (OutputStream os = conn.getOutputStream()) {
        os.write(jsonPayload.getBytes());
        os.flush();
      }

      return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
    } catch (IOException e) {
      return false;
    }
  }



  /**
   * popisKuhinja
   * 
   * Prolazi kroz sve postavke u datoteci i traži ključeve postavki s vrstom kuhinje. Za svaki ključ
   * vraća vrijednost te ga dodaje u kolekciju kuhinje.
   */
  public void popisKuhinja() {
    for (String kljuc : konfig.dajSvePostavke().stringPropertyNames()) {
      if (kljuc.startsWith("kuhinja_")) {
        String vrijednostKljuca = konfig.dajPostavku(kljuc);
        String[] dijelovi = vrijednostKljuca.split(";");
        if (dijelovi.length == 2) {

          String oznaka = kljuc;
          String vrsta = dijelovi[0];
          String naziv = dijelovi[1];
          kuhinje.put(oznaka, new Kuhinja(oznaka, vrsta, naziv));

        }
      }
    }

  }

  /**
   * ucitajKartuPica
   *
   * Učitavanje podataka iz datoteke s popisom pića u kolekciju kartaPica.
   *
   * @param nazivDatoteke - naziv datoteke s popisom dostupnih pića
   * @return true ako je uspješno učitana datoteka, false ako postoje problemi s pronalaskom
   *         datoteke ili čitanjem podataka.
   */
  public boolean ucitajKartuPica(String nazivDatoteke) {
    this.kartaPica = new ConcurrentHashMap<>();
    var datoteka = Path.of(nazivDatoteke);
    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return false;
    }
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var kartaPicaNiz = gson.fromJson(br, KartaPica[].class);
      var kartaPicaTok = Stream.of(kartaPicaNiz);
      kartaPicaTok.forEach(kp -> this.kartaPica.put(kp.id(), kp));
    } catch (IOException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
          "Greška kod spremanja karte pića: " + ex.getMessage());
      return false;
    }

    return true;
  }

  /**
   * Učitaj konfiguraciju - učitava konfiguracijske podatke iz datoteke nazivDatoteke
   *
   * @param nazivDatoteke naziv datoteke
   * @throws NeispravnaKonfiguracija - kod neispravne konfiguracije poziva se iznimka
   *         NeispravnaKonfiguracija.
   */
  public void ucitajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      this.datotekaObracuna = konfig.dajPostavku("datotekaObracuna");
      this.datotekaPartnera = konfig.dajPostavku("datotekaPartnera");
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
      throw ex;
    }
  }

  public void setRegistracijaAktivna(boolean aktivna) {
    this.registracijaAktivna = aktivna;
  }
}
