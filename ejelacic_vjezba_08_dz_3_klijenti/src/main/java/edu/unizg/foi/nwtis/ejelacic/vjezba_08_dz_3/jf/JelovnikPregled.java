package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jf;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@Named("jelovnikPregled")
@RequestScoped
public class JelovnikPregled implements Serializable {

  private Long partnerId;
  private Response jelovnik;

  // @Inject
  // ServisiPartnerKlijent servisiPartnerKlijent;
  //
  // @Inject
  // RestConfiguration restConfig;

  @PostConstruct
  public void init() {
    // Pretpostavimo da partnerId dolazi iz sesije ili URL parametra
    partnerId = getOdabraniPartnerId(); // mock metoda – u praksi iz sesije ili ViewParam

    if (partnerId != null) {
      // dohvat s REST API-ja
      // jelovnik = servisiPartnerKlijent.getJelovnik(null, null)

    }
  }

  // Getter
  // public List<Jelovnik> getJelovnik() {
  // return jelovnik;
  // }

  public Long getPartnerId() {
    return partnerId;
  }

  // Mock metoda
  private Long getOdabraniPartnerId() {
    // TODO: izvući iz sesije npr.

    return 5L; // testna vrijednost
  }
}


