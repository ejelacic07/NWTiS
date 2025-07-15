
package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jf;

import java.sql.Connection;
import java.sql.DriverManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class RestConfiguration {
  @Inject
  @ConfigProperty(name = "korisnickoImeBazaPodataka")
  private String korisnickoImeBazaPodataka;
  @Inject
  @ConfigProperty(name = "lozinkaBazaPodataka")
  private String lozinkaBazaPodataka;
  @Inject
  @ConfigProperty(name = "upravljacBazaPodataka")
  private String upravljacBazaPodataka;
  @Inject
  @ConfigProperty(name = "urlBazaPodataka")
  private String urlBazaPodataka;

  public Connection dajVezu() throws Exception {
    Class.forName(this.upravljacBazaPodataka);
    var vezaBazaPodataka = DriverManager.getConnection(this.urlBazaPodataka,
        this.korisnickoImeBazaPodataka, this.lozinkaBazaPodataka);
    return vezaBazaPodataka;
  }
}
