package Repository;

import Model.Enum.StatusImplantacao;
import Model.Implantacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImplantacaoRepository extends JpaRepository<Implantacao, Long> {


    List<Implantacao> findByStatus(StatusImplantacao status);
}
