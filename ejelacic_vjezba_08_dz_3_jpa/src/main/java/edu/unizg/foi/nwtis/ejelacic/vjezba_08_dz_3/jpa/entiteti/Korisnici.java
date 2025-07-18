package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;


/**
 * The persistent class for the KORISNICI database table.
 * 
 */
@Entity
@NamedQuery(name = "Korisnici.findAll", query = "SELECT k FROM Korisnici k")
public class Korisnici implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String korisnik;

  private String email;

  private String ime;

  private String lozinka;

  private String prezime;


  @ManyToMany
  @JoinTable(name = "ULOGE", joinColumns = {@JoinColumn(name = "KORISNIK")},
      inverseJoinColumns = {@JoinColumn(name = "GRUPA")})
  private List<Grupe> grupes;

  public Korisnici() {}

  public String getKorisnik() {
    return this.korisnik;
  }

  public void setKorisnik(String korisnik) {
    this.korisnik = korisnik;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getIme() {
    return this.ime;
  }

  public void setIme(String ime) {
    this.ime = ime;
  }

  public String getLozinka() {
    return this.lozinka;
  }

  public void setLozinka(String lozinka) {
    this.lozinka = lozinka;
  }

  public String getPrezime() {
    return this.prezime;
  }

  public void setPrezime(String prezime) {
    this.prezime = prezime;
  }

  public List<Grupe> getGrupes() {
    return this.grupes;
  }

  public void setGrupes(List<Grupe> grupes) {
    this.grupes = grupes;
  }

}
