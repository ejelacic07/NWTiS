package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


/**
 * The persistent class for the PARTNERI database table.
 * 
 */
@Entity
@Table(name = "PARTNERI")
@NamedQuery(name = "Partneri.findAll", query = "SELECT p FROM Partneri p")
public class Partneri implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private int id;

  @Column
  private String adminkod;

  @Column
  private String adresa;

  @Column
  private double gpsduzina;

  @Column
  private double gpssirina;

  @Column
  private int mreznavrata;

  @Column
  private int mreznavratakraj;

  @Column
  private String naziv;

  @Column
  private String sigurnosnikod;

  @Column
  private String vrstakuhinje;


  @OneToMany(mappedBy = "partneri")

  private List<Obracuni> obracunis;

  public Partneri() {}

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAdminkod() {
    return this.adminkod;
  }

  public void setAdminkod(String adminkod) {
    this.adminkod = adminkod;
  }

  public String getAdresa() {
    return this.adresa;
  }

  public void setAdresa(String adresa) {
    this.adresa = adresa;
  }

  public double getGpsduzina() {
    return this.gpsduzina;
  }

  public void setGpsduzina(double gpsduzina) {
    this.gpsduzina = gpsduzina;
  }

  public double getGpssirina() {
    return this.gpssirina;
  }

  public void setGpssirina(double gpssirina) {
    this.gpssirina = gpssirina;
  }

  public int getMreznavrata() {
    return this.mreznavrata;
  }

  public void setMreznavrata(int mreznavrata) {
    this.mreznavrata = mreznavrata;
  }

  public int getMreznavratakraj() {
    return this.mreznavratakraj;
  }

  public void setMreznavratakraj(int mreznavratakraj) {
    this.mreznavratakraj = mreznavratakraj;
  }

  public String getNaziv() {
    return this.naziv;
  }

  public void setNaziv(String naziv) {
    this.naziv = naziv;
  }

  public String getSigurnosnikod() {
    return this.sigurnosnikod;
  }

  public void setSigurnosnikod(String sigurnosnikod) {
    this.sigurnosnikod = sigurnosnikod;
  }

  public String getVrstakuhinje() {
    return this.vrstakuhinje;
  }

  public void setVrstakuhinje(String vrstakuhinje) {
    this.vrstakuhinje = vrstakuhinje;
  }

  public List<Obracuni> getObracunis() {
    return this.obracunis;
  }

  public void setObracunis(List<Obracuni> obracunis) {
    this.obracunis = obracunis;
  }

  public Obracuni addObracuni(Obracuni obracuni) {
    getObracunis().add(obracuni);
    obracuni.setPartneri(this);

    return obracuni;
  }

  public Obracuni removeObracuni(Obracuni obracuni) {
    getObracunis().remove(obracuni);
    obracuni.setPartneri(null);

    return obracuni;
  }

}
