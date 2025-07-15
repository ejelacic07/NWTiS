package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.NarudzbaStavka;

/**
 * The Class PosluziteljPartner.
 */
public class PosluziteljPartner {

  /** The narudzbe. */
  private Map<String, List<NarudzbaStavka>> narudzbe = new ConcurrentHashMap<>();

  /** Lista svih jelovnka. */
  private Map<String, Jelovnik> jelovnik = null;

  /** Lista karte pića. */
  private Map<String, KartaPica> kartaPica = null;

  /** Broj cekaca. */
  private int brojCekaca;

  /** Kolekcija s plaćenim narudžbama. */
  private Map<String, List<NarudzbaStavka>> placeneNarudzbe = new ConcurrentHashMap<>();

  /** The broj naplacenih. */
  private int brojNaplacenih = 0;

  /** Konfiguracijski podaci. */
  private Konfiguracija konfig;

  /** Kod za kraj rada. */
  private String kodZaKraj = "";

  /** Zastavica za kraj rada. */
  private AtomicBoolean zavrsi = new AtomicBoolean(false);

  /** adresa poslužitelja. */
  private String adresa = "";

  /** mrežna vrata za kraj. */
  private int mreznaVrataKraj;

  /** naziv partnera. */
  private String naziv = "";

  /** GPS širina partnera. */
  private double gpsSirina;

  /** GPS dužina partnera. */
  private double gpsDuzina;

  /** mrezna vrata. */
  private int mreznaVrata;

  /** mrezna vrata kraj partner **/
  private int mreznaVrataKrajPartner;

  /** Vrsta kuhinje. */
  private String kuhinja = "";

  /** Mrezna vrata za rad s partnerom. */
  private int mreznaVrataRad;

  /** Trajanje pauze dretve u ms. */
  private int pauzaDretve;

  /** ID partera. */
  private int id;

  /** mrežna vrata za registraciju. */
  private int mreznaVrataRegistracija;

  /** Sigurnosni kod partnera. */
  private String sigKod = "";

  /** Kvota narudžbi. */
  private int kvotaNarudzbi;

  /** Predložak za kraj. */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ$");

  /** Predlozak za rad. */
  private Pattern predlozakRad = Pattern.compile("^PARTNER$");

  /**
   * Getter za pribavljanje adrese.
   *
   * @return the adresa
   */
  public String getAdresa() {
    return this.adresa;
  }

  /** Pauza aktivna. */
  private boolean pauzaAktivna = false;

  /** Pauza kupca */
  private boolean pauzaKupca = false;


  /** Kod za admina. */
  private String kodZaAdmina;

  static Logger log = Logger.getLogger(PosluziteljPartner.class.getName());

  /**
   * Glavna metoda
   * 
   * Provjerava broj argumenata. Pokreće metode ucitajKonfiguraciju, zahtjevZaRegistraciju,
   * provjerava podudarnost regexa za kraj rada i za rad s partnerom.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {


    if (args.length > 2) {

      System.out.println("Broj argumenata je veći od 2.");
      return;
    }

    String nazivDatoteke = args[0];


    var partner = new PosluziteljPartner();


    if (!partner.ucitajKonfiguraciju(nazivDatoteke)) {

      return;
    }

    if (args.length == 1) {

      partner.zahtjevZaRegistraciju();
      partner.zahtjevZaRad();
      partner.registrirajMreznaVrataZaKrajPartner();
      return;
    }


    var linija = args[1];


    var poklapanje = partner.predlozakKraj.matcher(linija);
    boolean status = poklapanje.matches();
    if (status) {

      partner.posaljiKomanduKraj();
      return;
    }

    var matcher = partner.predlozakRad.matcher(linija);
    var statusRad = matcher.matches();
    if (statusRad) {

      partner.zahtjevZaRad();
      return;
    }

  }


  private void registrirajMreznaVrataZaKrajPartner() {

    AsynchronousServerSocketChannel serverChannel = null;
    try {
      serverChannel = AsynchronousServerSocketChannel.open()
          .bind(new InetSocketAddress(this.mreznaVrataKrajPartner));

      while (!zavrsi.get()) {

        Future<AsynchronousSocketChannel> futureKlijent = serverChannel.accept();
        AsynchronousSocketChannel klijent = futureKlijent.get();


        ByteBuffer buffer = ByteBuffer.allocate(2048);
        klijent.read(buffer).get();
        buffer.flip();

        String input = new String(buffer.array(), 0, buffer.limit());


        String[] dijelovi = input.trim().split("\\s+");
        String odgovor = "";
        if (dijelovi.length == 0) {

          odgovor = "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
        }
        String komanda = dijelovi[0].toUpperCase();

        switch (komanda) {
          case "KRAJ":
            odgovor = obradiKraj(dijelovi);
            break;
          case "STATUS":
            odgovor = obradiStatus(dijelovi);
            break;
          case "PAUZA":
            odgovor = obradiPauza(dijelovi);
            break;
          case "START":
            odgovor = obradiStart(dijelovi);
            break;
          case "SPAVA":
            odgovor = obradiSpava(dijelovi);
            break;
          case "OSVJEŽI":
            odgovor = obradiOsvjezi(dijelovi);
            break;
          default:

            odgovor = "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
        }
        ByteBuffer odgovorBuffer = ByteBuffer.wrap(odgovor.getBytes());
        klijent.write(odgovorBuffer).get();

        klijent.close();
      }
    } catch (IOException | InterruptedException | ExecutionException e) {

      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
          "Greška kod socketa za rad s krajem partnera: ", e);
    } finally {
      try {
        if (serverChannel != null && serverChannel.isOpen()) {

          serverChannel.close();
        }
      } catch (IOException e) {

        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
            "Greška kod zatvaranja socketa: ", e);
      }
    }
  }


  public String obradiKraj(String[] args) {

    if (args.length != 2 || !args[1].equals(kodZaKraj)) {

      return "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
    }
    this.zavrsi.set(true);

    return "OK";
  }

  public String obradiOsvjezi(String[] args) {

    if (args.length != 2 || !args[1].equals(kodZaAdmina)) {

      return "ERROR 61: Pogrešan kodZaAdminPartnera";
    }

    if (this.pauzaKupca) { // ?

      return "ERROR 62: Pogrešna promjena pauze ili starta";
    }


    ucitajJelovnik(adresa, mreznaVrataRad, id, sigKod);
    ucitajKartuPica(adresa, mreznaVrata, id, sigKod);
    return "OK";
  }


  public String obradiStatus(String[] args) {

    if (args.length != 3 || !args[1].equals(kodZaAdmina)) {

      return "ERROR 61: Pogrešan kodZaAdminPartnera";
    }
    int dio;
    try {
      dio = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {

      return "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
    }

    if (dio != 1) {

      return "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
    }


    return "OK " + (this.pauzaKupca ? "0" : "1");
  }

  public String obradiPauza(String[] args) {

    if (args.length != 3 || !args[1].equals(kodZaAdmina)) {

      return "ERROR 61: Pogrešan kodZaAdminPartnera";
    }

    int dio;
    try {
      dio = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {

      return "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
    }

    // 1?
    if (dio != 1 || this.pauzaKupca) {

      return "ERROR 62: Pogrešna promjena pauze ili starta";
    }

    this.pauzaKupca = true;

    return "OK";
  }

  public String obradiStart(String[] args) {

    if (args.length != 3 || !args[1].equals(kodZaAdmina)) {

      return "ERROR 61: Pogrešan kodZaAdminPartnera";
    }
    int dio;
    try {
      dio = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {

      return "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
    }
    // Startaj nije implementiran
    if (dio != 1 || this.pauzaKupca) {

      return "ERROR 62: Pogrešna promjena pauze ili starta";
    }
    this.pauzaKupca = false;

    return "OK";
  }

  public String obradiSpava(String[] args) {

    if (args.length != 3 || !args[1].equals(kodZaAdmina)) {

      return "ERROR 61: Pogrešan kodZaAdminPartnera";
    }

    int ms;
    try {
      ms = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {

      return "ERROR 60: Format komande nije ispravan ili nije ispravan kod za kraj";
    }

    try {

      Thread.sleep(ms);
    } catch (InterruptedException e) {

      return "ERROR 63: Prekid spavanja dretve";
    }

    return "OK";
  }


  /**
   * komandaRegistracija
   * 
   * Kreira komandu korištenjem StringBuilder klase.
   * 
   * @return vraća komandu tipa String
   */
  public String komandaRegistracija() {

    StringBuilder komanda = new StringBuilder();
    komanda.append("PARTNER").append(" ").append(this.id).append(" ")
        .append("\"" + this.naziv + "\"").append(" ").append(this.kuhinja).append(" ")
        .append(this.adresa).append(" ").append(this.mreznaVrataRegistracija).append(" ")
        .append(this.gpsSirina).append(" ").append(this.gpsDuzina).append(" ")
        .append(this.mreznaVrataKraj).append(" ").append(this.kodZaAdmina).append("\n");

    System.out.println("Komanda registracija " + komanda);

    return komanda.toString();
  }

  /**
   * zahtjevZaRegistraciju
   * 
   * Stvara mrežnu utičnicu na mrežnim vratima definiranima u konfiguracijskoj datoteci. Vraća
   * odgovor ili iznimku u slučaju pogreške kod čitanja. Sprema sigurnosni kod u konfiguracijsku
   * datoteku partnera.
   * 
   * 
   * @return vraća odgovor poslužitelja posluziteljTvrtka
   */
  public String zahtjevZaRegistraciju() { // obradi Kraj

    String odgovor = "";



    try (AsynchronousSocketChannel uticnicaKlijent = AsynchronousSocketChannel.open()) {
      log.info(
          "Connecting to registration socket " + this.adresa + ":" + this.mreznaVrataRegistracija);
      uticnicaKlijent.connect(new InetSocketAddress(this.adresa, this.mreznaVrataRegistracija))
          .get();

      String komanda = komandaRegistracija();


      ByteBuffer buffer = ByteBuffer.wrap(komanda.getBytes());
      uticnicaKlijent.write(buffer).get();

      ByteBuffer odgovorBuffer = ByteBuffer.allocate(2048);
      uticnicaKlijent.read(odgovorBuffer).get();
      odgovorBuffer.flip();

      odgovor = new String(odgovorBuffer.array(), 0, odgovorBuffer.limit());


      if (odgovor.startsWith("OK")) {
        sigKod = odgovor.substring(3);

        if (!konfig.postojiPostavka("sigKod")) {

          konfig.spremiPostavku("sigKod", sigKod);
        } else {

          konfig.azurirajPostavku("sigKod", sigKod);
        }
        konfig.spremiKonfiguraciju();
      }
    } catch (Exception e) {

      e.printStackTrace();
    }

    return odgovor;
  }

  /**
   * ucitajJelovnik
   *
   * Čita podatke, otvara mrežnu utičnicu te provjerava ispravnost komande. Sprema podatke iz tipa
   * JSON na listu Jelovnik.
   * 
   * @param adresa - adresa iz kofiguracijske datoteke
   * @param mreznaVrata - mrezna vrata za rad s partnerima
   * @param id - id partnera
   * @param sigKod - sigurnosni kod partnera
   * @return vraća true ako je jelovnik učitan, ako nije vraća false
   */
  private boolean ucitajJelovnik(String adresa, int mreznaVrataRad, int id, String sigKod) {

    jelovnik = new ConcurrentHashMap<String, Jelovnik>();
    try (Socket socket = new Socket(adresa, mreznaVrataRad);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      out.println("JELOVNIK " + id + " " + sigKod);

      StringBuilder jsonBuilder = new StringBuilder();
      String linija = null;
      while ((linija = in.readLine()) != null && !linija.isEmpty()) {
        jsonBuilder.append(linija);
      }
      String json = jsonBuilder.toString();
      if (!json.startsWith("OK")) {

        return false;
      }

      Gson gson = new Gson();
      int arrayStart = json.indexOf('[');
      String jsonArray = json.substring(arrayStart);
      Type listType = new TypeToken<List<Jelovnik>>() {}.getType();
      List<Jelovnik> itemList = gson.fromJson(jsonArray, listType);

      for (Jelovnik item : itemList) {
        jelovnik.put(item.id(), item);
      }

      return true;
    } catch (IOException e) {

      return false;
    }
  }

  /**
   * Ucitaj kartu pica.
   *
   *
   * Čita podatke, otvara mrežnu utičnicu te provjerava ispravnost komande. Sprema podatke iz tipa
   * JSON na listu kartaPica.
   * 
   * @param adresa - adresa iz kofiguracijske datoteke
   * @param mreznaVrata - mrezna vrata za rad s partnerima
   * @param id - id partnera
   * @param sigKod - sigurnosni kod partnera
   * @return vraća true ako je karta pića učitana, ako nije vraća false
   */
  private boolean ucitajKartuPica(String host, int mreznaVrata, int id, String sigKod) {

    kartaPica = new ConcurrentHashMap<String, KartaPica>();
    try (Socket socket = new Socket(host, mreznaVrata);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      out.println("KARTAPIĆA " + id + " " + sigKod);


      StringBuilder jsonBuilder = new StringBuilder();
      String linija;
      while ((linija = in.readLine()) != null && !linija.isEmpty()) {
        jsonBuilder.append(linija);
      }

      String json = jsonBuilder.toString();
      if (!json.startsWith("OK")) {

        return false;
      }

      Gson gson = new Gson();
      int arrayStart = json.indexOf('[');
      String jsonArray = json.substring(arrayStart);
      Type listType = new TypeToken<List<KartaPica>>() {}.getType();
      List<KartaPica> itemList = gson.fromJson(jsonArray, listType);

      for (KartaPica item : itemList) {
        kartaPica.put(item.id(), item);
      }

      return true;
    } catch (IOException | JsonSyntaxException e) {

      return false;
    }
  }

  /**
   * zahtjevZaRad
   * 
   * Provjerava postoji li sigurnosni kod i ako nije prazan String te jesu li učitani podaci o
   * jelovnicima i kartama pića.
   * 
   * Ako jesu pokreće novu virtualnu dretvu te otvara poslužiteljsku mrežnu utičnicu.
   * 
   * Poziva metodu obradiKlijenta.
   */
  private void zahtjevZaRad() {

    if (sigKod.isBlank() || sigKod.isEmpty()) {

      return;
    } // TODO vratiti adresu
    if (!ucitajJelovnik(adresa, mreznaVrataRad, id, sigKod)
        || !ucitajKartuPica(adresa, mreznaVrataRad, id, sigKod)) {

      return;
    }

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    executor.submit(() -> pripremiObraduKlijenta());
  }

  private void pripremiObraduKlijenta() {
    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(mreznaVrata);



      while (!zavrsi.get()) {

        Socket socket = serverSocket.accept();

        obradiKlijenta(socket);
        Thread.sleep(this.pauzaDretve);
      }
      serverSocket.close();

    } catch (IOException e) {

      e.printStackTrace();
    } catch (InterruptedException e) {

      e.printStackTrace();
    }
  }


  /**
   * obradiKlijenta.
   * 
   * 
   * Čita podatke od klijenta te poziva metode ovisno o tome koja komanda je stigla.
   * 
   * Ako komanda nije ispravna, ispisuje kod pogreške.
   * 
   * @param uticnicaKlijenta - mrezna utičnica za komunikaciju s klijentom.
   */
  private void obradiKlijenta(Socket uticnicaKlijenta) {

    try (
        BufferedReader in =
            new BufferedReader(new InputStreamReader(uticnicaKlijenta.getInputStream()));
        PrintWriter pisac = new PrintWriter(uticnicaKlijenta.getOutputStream(), true)) {

      String linija = in.readLine();
      if (linija == null) {

        return;
      }
      String[] dijeloviKomande = linija.trim().split(" ");


      switch (dijeloviKomande[0]) {
        case "JELOVNIK":

          obradiJelovnik(pisac, dijeloviKomande);
          break;
        case "KARTAPIĆA":

          obradiKartaPica(pisac, dijeloviKomande);
          break;
        case "NARUDŽBA":

          obradiNarudzba(pisac, dijeloviKomande);
          break;
        case "JELO":

          obradiJelo(pisac, dijeloviKomande);
          break;
        case "PIĆE":

          obradiPice(pisac, dijeloviKomande);
          break;
        case "RAČUN":

          obradiRacun(pisac, dijeloviKomande);
          break;
        case "STANJE":

          obradiKorisnika(pisac, dijeloviKomande);
        default:

          pisac.println("ERROR 40: Format komande nije ispravan");
      }
      pisac.flush();
    } catch (IOException e) {

      e.printStackTrace();
    }
  }

  private void obradiKorisnika(PrintWriter pisac, String[] dijeloviKomande) {

    if (pauzaAktivna) { // Di je poslana komanda?

      pisac.write("ERROR 48: Poslužitelj za prijem zahtjeva kupaca u pauzi");
      return;
    }
    String korisnik = dijeloviKomande[1];
    if (korisnik == null || korisnik.trim().isEmpty()) {

      pisac.write("ERROR : ");
      return;
    }

    List<NarudzbaStavka> narudzba = narudzbe.get(korisnik);
    Gson gson = new Gson();
    String jsonStavke = gson.toJson(narudzba);

    pisac.write("OK\n" + jsonStavke);
  }

  /**
   * obradiNarudzba
   * 
   * Ako nema trenutne narudžbe u kolekciji narudzbe, dodaje ju u kolekciju te ispisuje OK i popis
   * otvorenih narudzbi.
   *
   * @param pisac - PrintWriter za ispis odgovora.
   * @param dijeloviKomande - niz za provjeru postoji li narudzba u kolekciji.
   */
  private void obradiNarudzba(PrintWriter pisac, String[] dijeloviKomande) {

    if (dijeloviKomande.length == 2) {
      narudzbe.putIfAbsent(dijeloviKomande[1], new ArrayList<>());

      for (Map.Entry<String, List<NarudzbaStavka>> entry : narudzbe.entrySet()) {

        System.out.println(entry.getKey() + " = " + entry.getValue());
      }
      pisac.println("OK");
    } else {

      pisac.println("ERROR 20: Format komande nije ispravan");
    }
  }

  /**
   * obradiKartaPica
   * 
   * Ako je niz dijeloviKomande jednak 2, stvara JSON objekt na temelju liste kartaPica.
   *
   * @param pisac - PrintWriter za ispis odgovora.
   * @param dijeloviKomande - niz za provjeru veličine niza.
   */
  private void obradiKartaPica(PrintWriter pisac, String[] dijeloviKomande) {

    if (dijeloviKomande.length == 2) {
      pisac.println("OK " + new Gson().toJson(kartaPica));


    } else {

      pisac.println("ERROR 20: Format komande nije ispravan");
    }
  }

  /**
   * obradiKartaPica
   * 
   * Ako je niz dijeloviKomande jednak 2, stvara JSON objekt na temelju liste jelovnik
   *
   * @param pisac - PrintWriter za ispis odgovora.
   * @param dijeloviKomande - niz za provjeru veličine niza.
   */
  private void obradiJelovnik(PrintWriter pisac, String[] dijeloviKomande) {

    if (dijeloviKomande.length == 2) {
      pisac.println("OK " + new Gson().toJson(jelovnik));

    } else {

      pisac.println("ERROR 40: Format komande nije ispravan");
    }
  }

  /**
   * obradiRacun
   * 
   * Ako je pristigla komanda za obračun, narudžba se briše iz kolekcije narudzbe.
   * 
   * Dodaje narudžbu u kolekciju placeneNarudzbe i povećava broj broj naplaćenih narudzbi.
   * 
   * Stvara listu obracun na koju dodaje stavke iz liste placeneNarudbe i briše podatke iz liste.
   * 
   * Ispisuje OK.
   * 
   * 
   * @param pisac - PrintWriter za ispis odgovora.
   * @param dijeloviKomande - niz za provjeru veličine niza.
   * 
   * 
   * 
   *        }
   * 
   */
  private void obradiRacun(PrintWriter pisac, String[] dijeloviKomande) {

    if (dijeloviKomande.length == 2) {
      String korisnik = dijeloviKomande[1];
      List<NarudzbaStavka> narudzba = narudzbe.remove(korisnik);

      if (narudzba == null) {

        pisac.println("ERROR 43: Ne postoji otvorena narudžba za kupca");
        return;
      }

      placeneNarudzbe.putIfAbsent(korisnik, new ArrayList<>());
      placeneNarudzbe.get(korisnik).addAll(narudzba);
      brojNaplacenih++;


      List<NarudzbaStavka> obracun = new ArrayList<>();

      if (brojNaplacenih % this.kvotaNarudzbi == 0) {

        System.out.println("Modulo je 0");

        Map<String, Double> agregat = new HashMap<>();
        for (List<NarudzbaStavka> stavke : placeneNarudzbe.values()) {
          for (NarudzbaStavka s : stavke) {
            double iznos = 0.0;
            switch (s.tip()) {
              case "JELO":
                Jelovnik stavkaJelovnik = jelovnik.get(s.idArtikla());
                if (stavkaJelovnik != null)
                  iznos = s.kolicina() * stavkaJelovnik.cijena();
                break;
              case "PICE":
                KartaPica stavkaPica = kartaPica.get(s.idArtikla());
                if (stavkaPica != null)
                  iznos = s.kolicina() * stavkaPica.cijena();

                System.out.println("Količina " + s.kolicina());
                break;
            }
            if (s.idArtikla().equals("p13")) {
              System.out.println("Čaj količina" + s.kolicina() + " Ciijena "
                  + kartaPica.get(s.idArtikla()).cijena());
            }
            agregat.merge(s.idArtikla(), iznos, Double::sum);
          }
        }
        for (Map.Entry<String, Double> e : agregat.entrySet()) {
          obracun.add(new NarudzbaStavka(e.getKey(), e.getValue(), "OBRACUN"));
          System.out.println("Agregat je " + e.toString());
        }
        placeneNarudzbe.clear();
        posaljiObracun(obracun);
      }
      pisac.println("OK");
    } else {

      pisac.println("ERROR 40: Format komande nije ispravan");
    }
  }

  private boolean posaljiObracun(List<NarudzbaStavka> obracun) {

    try (Socket socket = new Socket(this.adresa, mreznaVrataRad);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      String komanda = "OBRAČUN" + " " + id + " " + sigKod + new Gson().toJson(obracun);
      out.println(komanda);
      String odgovor = in.readLine();

      if (!"OK".equals(odgovor)) {

        return false;
      }

      return true;
    } catch (IOException | JsonSyntaxException e) {

      return false;
    }
  }

  /**
   * obradiPice
   * 
   * Rastavljaju se dijelovi komande na elemente niza te se radi provjera postoji li id tog pića u
   * kolekciji kartaPica.
   * 
   * Ako postoji, piće se dodaje u kolekciju narudzbe i ispisuje se OK.
   *
   * @param pisac - PrintWriter za ispis odgovora
   * @param dijeloviKomande - niz koji predstavlja dijelove primljene komande
   */
  private void obradiPice(PrintWriter pisac, String[] dijeloviKomande) {

    if (dijeloviKomande.length == 4) {
      String korisnik = dijeloviKomande[1];
      String idPica = dijeloviKomande[2];
      double kolicina = Double.parseDouble(dijeloviKomande[3]);

      if (!narudzbe.containsKey(korisnik)) {

        pisac.println("ERROR 43: Ne postoji otvorena narudžba za kupca");
        return;
      }

      boolean postoji = kartaPica.containsKey(idPica);
      if (!postoji) {

        pisac.println("ERROR 42: Ne postoji piće s tim ID-jem");
        return;
      }

      narudzbe.get(korisnik).add(new NarudzbaStavka(idPica, kolicina, "PICE"));

      pisac.println("OK");
    } else {

      pisac.println("ERROR 40: Format komande nije ispravan");
    }
  }

  /**
   * obradiJelo
   * 
   * Rastavljaju se dijelovi komande na elemente niza te se radi provjera postoji li id tog Jela u
   * kolekciji jelovnik.
   * 
   * Ako postoji, piće se dodaje u kolekciju narudzbe i ispisuje se OK.
   *
   * @param pisac - PrintWriter za ispis odgovora
   * @param dijeloviKomande - niz koji predstavlja dijelove primljene komande
   */
  private void obradiJelo(PrintWriter pisac, String[] dijelovi) {

    if (dijelovi.length == 4) {
      String korisnik = dijelovi[1];
      String idJela = dijelovi[2];
      double kolicina = Double.parseDouble(dijelovi[3]);

      if (!narudzbe.containsKey(korisnik)) {

        pisac.println("ERROR 43: Ne postoji otvorena narudžba za kupca");
        return;
      }

      boolean postoji = jelovnik.containsKey(idJela);
      if (!postoji) {

        pisac.println("ERROR 41: Ne postoji jelo s tim ID-jem");
        return;
      }

      narudzbe.get(korisnik).add(new NarudzbaStavka(idJela, kolicina, "JELO"));

      pisac.println("OK");
    } else {

      pisac.println("ERROR 40: Format komande nije ispravan");
    }
  }

  /**
   * posaljiKomanduZaKraj
   **
   */
  public void posaljiKomanduKraj() {

    try (Socket mreznaUticnica = new Socket(this.adresa, this.mreznaVrataKraj);
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"), true);
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"))) {
      out.write("KRAJ " + kodZaKraj + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();

      String odgovorPosluzitelja = in.readLine();

      if (odgovorPosluzitelja.equals("OK")) {

        zavrsi.set(true);
      }
      mreznaUticnica.close();

    } catch (IOException e) {
      e.getStackTrace();
    }
  }

  /**
   * UcitajKonfiguraciju
   * 
   * Učitava postavke iz konfiguracijske datoteke.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije, ako nije vraća false
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      adresa = konfig.dajPostavku("adresa");
      brojCekaca = Integer.valueOf(konfig.dajPostavku("brojCekaca"));
      gpsSirina = Double.valueOf(konfig.dajPostavku("gpsSirina"));
      gpsDuzina = Double.valueOf(konfig.dajPostavku("gpsDuzina"));
      id = Integer.valueOf(konfig.dajPostavku("id"));
      kodZaKraj = konfig.dajPostavku("kodZaKraj");
      kuhinja = konfig.dajPostavku("kuhinja");
      mreznaVrata = Integer.valueOf(konfig.dajPostavku("mreznaVrata"));
      mreznaVrataKraj = Integer.valueOf(konfig.dajPostavku("mreznaVrataKraj"));
      mreznaVrataRad = Integer.valueOf(konfig.dajPostavku("mreznaVrataRad"));
      mreznaVrataRegistracija = Integer.valueOf(konfig.dajPostavku("mreznaVrataRegistracija"));
      naziv = konfig.dajPostavku("naziv");
      pauzaDretve = Integer.valueOf(konfig.dajPostavku("pauzaDretve"));
      kvotaNarudzbi = Integer.valueOf(konfig.dajPostavku("kvotaNarudzbi"));
      mreznaVrataKrajPartner = Integer.valueOf(konfig.dajPostavku("mreznaVrataKrajPartner"));
      sigKod = konfig.dajPostavku("sigKod");
      kodZaAdmina = konfig.dajPostavku("kodZaAdminPartnera");
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public void setPauzaAktivna(boolean pauzaAktivna) {
    this.pauzaAktivna = pauzaAktivna;
  }
}
