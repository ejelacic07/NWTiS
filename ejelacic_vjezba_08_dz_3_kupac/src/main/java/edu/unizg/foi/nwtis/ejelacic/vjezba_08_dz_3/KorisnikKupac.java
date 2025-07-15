package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;


/**
 * Klasa klijenta KorisnikKupac.
 */
public class KorisnikKupac {

  /** Konfiguracijski podaci */
  private Konfiguracija konfig;

  /** Lokalna adresa. */
  private String adresa = "";

  /** Mrežna vrata poslužitelja. */
  private int mreznaVrata;

  /** linija pročitana iz .csv datoteke */
  private String linija;

  /** Ime korisnik. */
  private String korisnik;

  /** Dužina spavanja dretve */
  private int spavanje;

  /** Komanda pročitana iz datoteke */
  private String komanda;



  /**
   * Glavna metoda - pokreće metode za učitavanje konfiguracijskih podataka i podataka za komandu
   *
   * @param argumenti - datoteka postavki i datoteka podacima za komandu
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Broj argumenata nije 2");
      return;
    }

    KorisnikKupac korisnik = new KorisnikKupac();
    korisnik.ucitajKonfiguraciju(args[0]);


    korisnik.ucitajPodatke(args[1]);
  }



  /**
   * Ucitaj podatke - učitava podatke potrebne za slanje iz datoteke
   *
   * @param nazivDatoteke - naziv datoteke koja se otvara i čita
   */
  public void ucitajPodatke(String nazivDatoteke) {

    try (BufferedReader citac = new BufferedReader(new FileReader(nazivDatoteke))) {
      while ((linija = citac.readLine()) != null) {
        var elementi = linija.split(";");
        if (elementi.length != 5) {
          continue;
        }
        this.korisnik = elementi[0];
        this.adresa = elementi[1];
        this.mreznaVrata = Integer.parseInt(elementi[2]);
        this.spavanje = Integer.parseInt(elementi[3]);
        this.komanda = elementi[4];
        kreirajKomandu();
      }

    } catch (IOException ex) {
      Logger.getLogger("Problem s čitanjem podataka").log(Level.SEVERE, null, ex);
    } catch (NumberFormatException e) {
      System.out.println("Greška kod parsanja broja!");
      Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
    }
  }


  /**
   * Kreiraj komandu - kreira i šalje komandu te prima odgovor poslužitelja PosluziteljPartner
   *
   */
  public void kreirajKomandu() {

    try (Socket mreznaUticnica = new Socket(adresa, this.mreznaVrata);
        PrintWriter pisac = new PrintWriter(mreznaUticnica.getOutputStream(), true);
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream()))) {

      komanda = komanda + "\n";
      System.out.println(komanda);

      Thread.sleep(spavanje);
      pisac.println(komanda);
      String odgovor = in.readLine();
      System.out.println("Odgovor: " + odgovor);

      if ("OK".equals(odgovor)) {
        StringBuilder jsonBuilder = new StringBuilder();
        String dodatni = "";
        String linija;
        while ((linija = in.readLine()) != null && !linija.isEmpty()) {
          jsonBuilder.append(linija);
        }
        dodatni = jsonBuilder.toString();
        if (!dodatni.isBlank() && !dodatni.isEmpty()) {
          System.out.println("Dodatni " + dodatni);
        }
      }

    } catch (Exception e) {
      System.out.println("Greška u obradi komande: " + komanda);
      Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
    }
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);

      this.adresa = konfig.dajPostavku("adresa");
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
