package banco.modelo;

import banco.excecao.BancoException;

/** INTERFACE: capacidade de render juros. Quem a implementa precisa definir como. */
public interface Rentavel {
    void aplicarRendimento() throws BancoException;
}
