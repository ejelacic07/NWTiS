package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the GRUPE database table.
 * 
 */
@Entity
@NamedQuery(name="Grupe.findAll", query="SELECT g FROM Grupe g")
public class Grupe implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String grupa;

	private String naziv;

	//bi-directional many-to-many association to Korisnici
	@ManyToMany(mappedBy="grupes")
	private List<Korisnici> korisnicis;

	public Grupe() {
	}

	public String getGrupa() {
		return this.grupa;
	}

	public void setGrupa(String grupa) {
		this.grupa = grupa;
	}

	public String getNaziv() {
		return this.naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public List<Korisnici> getKorisnicis() {
		return this.korisnicis;
	}

	public void setKorisnicis(List<Korisnici> korisnicis) {
		this.korisnicis = korisnicis;
	}

}