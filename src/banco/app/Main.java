package banco.app;

import banco.excecao.BancoException;
import banco.excecao.PersistenciaException;
import banco.excecao.ValorInvalidoException;
import banco.modelo.Conta;
import banco.repositorio.ContaRepositorioMySQL;
import banco.servico.BancoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner entrada = new Scanner(System.in);

    public static void main(String[] args) {
        // Único lugar que escolhe a implementação de armazenamento.
        // Com banco de dados, basta trocar esta linha.
        BancoService banco = new BancoService(new ContaRepositorioMySQL());

        String opcao;
        do {
            exibirMenu();
            opcao = entrada.nextLine().trim();
            try {
                executar(opcao, banco);
            } catch (BancoException e) {
                System.out.println("[ERRO] " + e.getMessage());
            } catch (PersistenciaException e) {
                System.out.println("[ERRO DE BANCO DE DADOS] :" + e.getMessage());
            }
        } while (!opcao.equals("0"));
        System.out.println("Encerrando.");
    }

    private static void exibirMenu() {
        System.out.println("\n===== BANCO =====");
        System.out.println("1 - Abrir conta");
        System.out.println("2 - Depositar");
        System.out.println("3 - Sacar");
        System.out.println("4 - Transferir");
        System.out.println("5 - Consultar saldo");
        System.out.println("6 - Listar contas");
        System.out.println("7 - Fechamento mensal");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");
    }

    private static void executar(String opcao, BancoService banco) throws BancoException {
        switch (opcao) {
            case "1": {
                String titular = ler("Titular: ");
                String tipo = ler("Tipo (1-Corrente, 2-Poupança): ");
                Conta conta = banco.abrirConta(titular, tipo.equals("2"));
                System.out.println("Conta aberta: " + conta);
                break;
            }
            case "2":
                banco.depositar(ler("Nº da conta: "), lerValor("Valor: "));
                System.out.println("Depósito realizado.");
                break;
            case "3":
                banco.sacar(ler("Nº da conta: "), lerValor("Valor: "));
                System.out.println("Saque realizado.");
                break;
            case "4":
                banco.transferir(ler("Conta de origem: "), ler("Conta de destino: "), lerValor("Valor: "));
                System.out.println("Transferência realizada.");
                break;
            case "5":
                System.out.println("Saldo: R$ " + banco.buscar(ler("Nº da conta: ")).getSaldo());
                break;
            case "6": {
                List<Conta> contas = banco.listar();
                if (contas.isEmpty()) {
                    System.out.println("Nenhuma conta aberta.");
                }
                for (Conta c : contas) {
                    System.out.println(c);
                }
                break;
            }
            case "7": {
                List<String> relatorio = banco.fecharMes();
                for (String linha : relatorio) {
                    System.out.println(linha);
                }
                break;
            }
            case "0":
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private static String ler(String texto) {
        System.out.print(texto);
        return entrada.nextLine().trim();
    }

    private static BigDecimal lerValor(String texto) throws ValorInvalidoException {
        try {
            return new BigDecimal(ler(texto).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValorInvalidoException("Digite um valor numérico válido.");
        }
    }
}
