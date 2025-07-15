package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.podaci.Korisnik;



public class KorisnikDAO {
  private Connection vezaBP;

  public KorisnikDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }



  public boolean provjeriKorisnika(String korisnickoIme, String lozinka) {
    String upit = "SELECT COUNT(*) FROM korisnici WHERE korisnik = ? AND lozinka = ?";
    try (Connection con = vezaBP; PreparedStatement stmt = con.prepareStatement(upit)) {

      stmt.setString(1, korisnickoIme);
      stmt.setString(2, lozinka);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    } catch (Exception e) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, e);
    }
    return false;
  }



  public Korisnik dajKorsnika(String id) {
    String upit = "SELECT * FROM korisnici WHERE korisnik = ?";
    try (Connection con = vezaBP; PreparedStatement stmt = con.prepareStatement(upit)) {

      stmt.setString(1, String.valueOf(id));
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        Korisnik k = new Korisnik(rs.getString("korisnik"), rs.getString("lozinka"),
            rs.getString("prezime"), rs.getString("ime"), rs.getString("email"));
        return k.korisnikBezLozinke();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }



  public List<Korisnik> dajSveKorisnike() {
    List<Korisnik> lista = new ArrayList<>();
    String upit = "SELECT * FROM korisnici";
    try (Connection con = vezaBP;
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(upit)) {

      while (rs.next()) {
        Korisnik k = new Korisnik(rs.getString("korisnik"), rs.getString("lozinka"),
            rs.getString("prezime"), rs.getString("ime"), rs.getString("email"));
        lista.add(k.korisnikBezLozinke());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lista;
  }



  public boolean dodajKorisnika(Korisnik korisnik) {

    String upit =
        "INSERT INTO korisnici (korisnik, lozinka,prezime, ime, email) VALUES (?, ?, ?, ?, ?)";
    try (Connection con = vezaBP; PreparedStatement stmt = con.prepareStatement(upit)) {

      stmt.setString(1, korisnik.korisnik());
      stmt.setString(2, korisnik.lozinka());
      stmt.setString(3, korisnik.prezime());
      stmt.setString(4, korisnik.ime());
      stmt.setString(5, korisnik.email());

      int brojRedova = stmt.executeUpdate();
      return brojRedova == 1;

    } catch (SQLException e) {
      System.err.println("Greška prilikom dodavanja korisnika: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  public Korisnik dohvati(String korisnik, String lozinka, Boolean prijava) {
    String upit = "SELECT ime, prezime, korisnik, lozinka, email FROM korisnici WHERE korisnik = ?";

    if (prijava) {
      upit += " and lozinka = ?";
    }
    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      if (prijava) {
        s.setString(2, lozinka);
      }
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");

        Korisnik k = new Korisnik(korisnik, "******", prezime, ime, email);
        return k;
      }

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }


  public List<Korisnik> dohvatiPrezimeIme(String pPrezime, String pIme) {
    String upit =
        "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici WHERE prezime LIKE ? AND ime LIKE ?";

    List<Korisnik> korisnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit);) {

      s.setString(1, pPrezime);
      s.setString(2, pIme);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      rs.close();
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean azuriraj(Korisnik k, String lozinka) {
    String upit = "UPDATE korisnici SET ime = ?, prezime = ?, email = ?, lozinka = ? "
        + " WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, lozinka);
      s.setString(5, k.korisnik());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }


}
