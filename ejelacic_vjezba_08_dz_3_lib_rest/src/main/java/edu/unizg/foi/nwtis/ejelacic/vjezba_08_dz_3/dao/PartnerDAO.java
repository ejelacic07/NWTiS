package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.VrstaObracuna;


public class PartnerDAO {
  private Connection vezaBP;

  Logger log = Logger.getLogger(this.getClass().getName());

  public PartnerDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public Partner dohvati(int id, boolean sakriKodove) {
    String upit =
        "SELECT naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod FROM partneri WHERE id = ?";
    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setInt(1, id);

      ResultSet rs = s.executeQuery();
      while (rs.next()) {
        String naziv = rs.getString("naziv");
        String vrstaKuhinje = rs.getString("vrstaKuhinje");
        String adresa = rs.getString("adresa");
        int mreznaVrata = rs.getInt("mreznaVrata");
        int mreznaVrataKraj = rs.getInt("mreznaVrataKraj");
        float gpsSirina = rs.getFloat("gpsSirina");
        float gpsDuzina = rs.getFloat("gpsDuzina");
        String sigurnosniKod = rs.getString("sigurnosniKod");
        String adminKod = rs.getString("adminKod");

        Partner p = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj,
            gpsSirina, gpsDuzina, sigurnosniKod, adminKod);
        if (sakriKodove) {
          p = p.partnerBezKodova();
        }
        return p;
      }

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }


  public List<Partner> dohvatiSve(boolean sakriKodove) {
    String upit =
        "SELECT id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod FROM partneri ORDER BY id";

    List<Partner> partneri = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        int id = rs.getInt("id");
        String naziv = rs.getString("naziv");
        String vrstaKuhinje = rs.getString("vrstaKuhinje");
        String adresa = rs.getString("adresa");
        int mreznaVrata = rs.getInt("mreznaVrata");
        int mreznaVrataKraj = rs.getInt("mreznaVrataKraj");
        float gpsSirina = rs.getFloat("gpsSirina");
        float gpsDuzina = rs.getFloat("gpsDuzina");
        String sigurnosniKod = rs.getString("sigurnosniKod");
        String adminKod = rs.getString("adminKod");

        Partner p = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj,
            gpsSirina, gpsDuzina, sigurnosniKod, adminKod);
        if (sakriKodove) {
          p = p.partnerBezKodova();
        }
        partneri.add(p);
      }
      return partneri;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }


  public boolean azuriraj(Partner p) {
    String upit =
        "UPDATE partneri SET naziv = ?, adresa = ?, mreznaVrata = ?, mreznaVrataKraj = ?, gpsSirina = ?, gpsDuzina = ?, sigurnosniKod = ?, adminKod = ? "
            + " WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, p.naziv());
      s.setString(2, p.adresa());
      s.setInt(3, p.mreznaVrata());
      s.setInt(4, p.mreznaVrataKraj());
      s.setFloat(5, p.gpsSirina());
      s.setFloat(6, p.gpsDuzina());
      s.setString(7, p.sigurnosniKod());
      s.setString(8, p.adminKod());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }


  public boolean dodaj(Partner p) {

    String upit =
        "INSERT INTO partneri (id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setInt(1, p.id());
      s.setString(2, p.naziv());
      s.setString(3, p.vrstaKuhinje());
      s.setString(4, p.adresa());
      s.setInt(5, p.mreznaVrata());
      s.setInt(6, p.mreznaVrataKraj());
      s.setFloat(7, p.gpsSirina());
      s.setFloat(8, p.gpsDuzina());
      s.setString(9, p.sigurnosniKod());
      s.setString(10, p.adminKod());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }


  public List<Partner> provjera(String status) {
    List<Partner> rezultat = new ArrayList<>();

    try {
      int pocetak = status.indexOf('[');
      int kraj = status.lastIndexOf(']');

      if (pocetak == -1 || kraj == -1 || kraj <= pocetak) {
        return rezultat;
      }

      String jsonNiz = status.substring(pocetak, kraj + 1);

      Gson gson = new Gson();
      Type partnerListType = new TypeToken<List<Partner>>() {}.getType();
      List<Partner> partneriIzStatusa = gson.fromJson(jsonNiz, partnerListType);
      List<Partner> partneriIzBaze = dohvatiSve(true);

      for (Partner pStatus : partneriIzStatusa) {
        for (Partner pBaza : partneriIzBaze) {
          if (pStatus.id() == pBaza.id()) {
            rezultat.add(pBaza); // ili pStatus
            break;
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Greška pri provjeri partnera: " + e.getMessage());
    }

    return rezultat;
  }


  public List<Obracun> obradiObracun(String od, String doVrijeme, VrstaObracuna tip) {
    List<Obracun> rezultat = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT * FROM obracuni WHERE 1=1");

    switch (tip) {
      case JELA -> sql.append(" AND jelo = true");
      case PIĆA -> sql.append(" AND jelo = false");
      default -> {
      }
    }

    if (od != null && !od.isBlank()) {
      sql.append(" AND vrijeme >= ?");
    }
    if (doVrijeme != null && !doVrijeme.isBlank()) {
      sql.append(" AND vrijeme <= ?");
    }

    try (Connection con = vezaBP; PreparedStatement ps = con.prepareStatement(sql.toString())) {
      int index = 1;

      if (od != null && !od.isBlank()) {
        long millisOd = Long.parseLong(od);
        ps.setTimestamp(index++, new Timestamp(millisOd));
      }

      if (doVrijeme != null && !doVrijeme.isBlank()) {
        long millisDo = Long.parseLong(doVrijeme);
        ps.setTimestamp(index++, new Timestamp(millisDo));
      }

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        Timestamp ts = rs.getTimestamp("vrijeme");
        long vrijemeLong = (ts != null) ? ts.getTime() : 0L;

        Obracun o = new Obracun(rs.getInt("rb"), rs.getInt("partner"), rs.getString("id"),
            rs.getBoolean("jelo"), rs.getFloat("kolicina"), rs.getFloat("cijena"), vrijemeLong);

        rezultat.add(o);
      }

    } catch (Exception e) {
      System.err.println("Greška prilikom dohvaćanja obračuna: " + e.getMessage());
    }

    return rezultat;
  }


  public boolean dodajObracun(List<Obracun> jsonObracun) {
    int uk = 0;
    try (Connection con = vezaBP) {
      con.setAutoCommit(false);
      String sql =
          "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) VALUES (?, ?, ?, ?, ?, ?)";
      try (PreparedStatement ps = con.prepareStatement(sql)) {
        for (Obracun obracun : jsonObracun) {
          ps.setInt(1, obracun.partner());
          ps.setString(2, obracun.id());
          ps.setBoolean(3, obracun.jelo());
          ps.setFloat(4, obracun.kolicina());
          ps.setFloat(5, obracun.cijena());
          ps.setTimestamp(6, new Timestamp(obracun.vrijeme()));
          int rows = ps.executeUpdate();
          if (rows == 1) {
            uk++;
            log.info("Uspješno dodan obračun: " + obracun.toString());
          } else {
            log.warning("Nije dodan obračun: " + obracun.toString());
          }
        }
        con.commit();
      }
    } catch (Exception e) {
      log.severe("Greška pri dodavanju obračuna: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
    log.info("Ukupno dodanih obračuna: " + uk);
    return uk == jsonObracun.size();
  }



  public List<Obracun> dohvatiObracunPoPartneru(int id, String od, String doVrijeme) {
    List<Obracun> rezultat = new ArrayList<>();

    StringBuilder sql = new StringBuilder("SELECT * FROM obracuni WHERE 1=1");
    sql.append(" AND partner=" + id);
    if (od != null && !od.isBlank()) {
      sql.append(" AND vrijeme >= ?");
    }
    if (doVrijeme != null && !doVrijeme.isBlank()) {
      sql.append(" AND vrijeme <= ?");
    }

    try (Connection con = vezaBP; PreparedStatement ps = con.prepareStatement(sql.toString())) {
      int index = 1;
      if (od != null && !od.isBlank()) {
        ps.setLong(index++, Long.parseLong(od));
      }
      if (doVrijeme != null && !doVrijeme.isBlank()) {
        ps.setLong(index++, Long.parseLong(doVrijeme));
      }

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        Obracun o = new Obracun(rs.getInt("rb"), rs.getInt("partner"), rs.getString("id"),
            rs.getBoolean("jelo"), rs.getFloat("kolicina"), rs.getFloat("cijena"),
            rs.getLong("vrijeme"));
        rezultat.add(o);
      }

    } catch (Exception e) {
      System.err.println("Greška prilikom dohvaćanja obračuna: " + e.getMessage());
    }

    return rezultat;
  }


}
