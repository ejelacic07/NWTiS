package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@RequestScoped
@Named("kreirajNarudzbu")
public class KreirajNarudzbu implements Serializable {
  // @Inject
  // private GlobalniPodaci globalniPodaci;

  Logger log = Logger.getLogger(this.getClass().getName());

  private List<Jelovnik> jelovnik;
  private List<KartaPica> kartaPica;

  // --- Podaci o narudžbi
  private Long odabranoJelo;
  private Long odabranoPice;
  private int kolicinaJelo = 1;
  private int kolicinaPice = 1;

  private List<Narudzba> narudzba;

  private boolean aktivnaNarudzba;

  /**
   * @PostConstruct public void init() { // Inicijalno punjenje mock podataka (zamijeniti REST
   *                dohvatima)
   * 
   * 
   * 
   *                }
   */

  public void dodajJelo() {
    narudzba.add(new Narudzba("1", "1", Boolean.TRUE, 1, 1, 1));
    log.info(narudzba.toString());
  }


}
