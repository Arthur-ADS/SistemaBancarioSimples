package banco.app;

import banco.excecao.BancoException;
import banco.excecao.PersistenciaException;
import banco.excecao.ValorInvalidoException;
import banco.modelo.Cliente;
import banco.modelo.Conta;
import banco.modelo.Endereco;
import banco.modelo.Movimentacao;
import banco.modelo.Servico;
import banco.modelo.StatusConta;
import banco.repositorio.ClienteRepositorioJPA;
import banco.repositorio.ContaRepositorioJPA;
import banco.repositorio.JPAUtil;
import banco.repositorio.ServicoRepositorioJPA;
import banco.servico.BancoService;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner entrada = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"));

    public static void main(String[] args) {
        // Único lugar que escolhe a implementação de armazenamento (agora: Hibernate/JPA).
        BancoService banco = new BancoService(
                new ContaRepositorioJPA(), new ClienteRepositorioJPA(), new ServicoRepositorioJPA());

        try {
            banco.inicializarCatalogoServicos();
        } catch (PersistenciaException e) {
            System.out.println("[ERRO DE BANCO DE DADOS] " + e.getMessage());
            return;
        }

        String opcao;
        do {
            exibirMenu();
            opcao = entrada.nextLine().trim();
            try {
                executar(opcao, banco);
            } catch (BancoException e) {
                System.out.println("[ERRO] " + e.getMessage());
            } catch (PersistenciaException e) {
                System.out.println("[ERRO DE BANCO DE DADOS] " + e.getMessage());
            }
        } while (!opcao.equals("0"));

        JPAUtil.fechar();
        System.out.println("Encerrando.");
    }

    private static void exibirMenu() {
        System.out.println("\n===== BANCO =====");
        System.out.println(" 1 - Cadastrar cliente");
        System.out.println(" 2 - Listar clientes e suas contas");
        System.out.println(" 3 - Abrir conta");
        System.out.println(" 4 - Depositar");
        System.out.println(" 5 - Sacar");
        System.out.println(" 6 - Transferir");
        System.out.println(" 7 - Extrato");
        System.out.println(" 8 - Listar contas");
        System.out.println(" 9 - Contratar serviço");
        System.out.println("10 - Bloquear / desbloquear / encerrar conta");
        System.out.println("11 - Fechamento mensal");
        System.out.println(" 0 - Sair");
        System.out.print("Opção: ");
    }

    private static void executar(String opcao, BancoService banco) throws BancoException {
        switch (opcao) {
            case "1": {
                String cpf = ler("CPF: ");
                String nome = ler("Nome: ");
                Endereco endereco = new Endereco(ler("Logradouro: "), ler("Cidade: "),
                        ler("UF: ").toUpperCase(), ler("CEP: ").replaceAll("\\D", ""));
                Cliente cliente = banco.cadastrarCliente(cpf, nome, endereco);
                System.out.println("Cliente cadastrado: " + cliente);
                break;
            }
            case "2": {
                List<Cliente> clientes = banco.listarClientes();
                if (clientes.isEmpty()) {
                    System.out.println("Nenhum cliente cadastrado.");
                }
                for (Cliente c : clientes) {
                    System.out.println(c);
                    for (Conta conta : c.getContas()) { // navegação Cliente -> Contas (lado mappedBy)
                        System.out.println("    " + conta);
                    }
                }
                break;
            }
            case "3": {
                String cpf = ler("CPF do titular: ");
                String tipo = ler("Tipo (1-Corrente, 2-Poupança): ");
                Conta conta = banco.abrirConta(cpf, tipo.equals("2"));
                System.out.println("Conta aberta: " + conta);
                break;
            }
            case "4":
                banco.depositar(ler("Nº da conta: "), lerValor("Valor: "));
                System.out.println("Depósito realizado.");
                break;
            case "5":
                banco.sacar(ler("Nº da conta: "), lerValor("Valor: "));
                System.out.println("Saque realizado.");
                break;
            case "6":
                banco.transferir(ler("Conta de origem: "), ler("Conta de destino: "), lerValor("Valor: "));
                System.out.println("Transferência realizada.");
                break;
            case "7":
                imprimirExtrato(banco.buscar(ler("Nº da conta: ")));
                break;
            case "8": {
                List<Conta> contas = banco.listar();
                if (contas.isEmpty()) {
                    System.out.println("Nenhuma conta aberta.");
                }
                for (Conta c : contas) {
                    System.out.println(c);
                }
                break;
            }
            case "9": {
                for (Servico s : banco.listarServicos()) {
                    System.out.println("  " + s);
                }
                banco.contratarServico(ler("Nº da conta: "), ler("Código do serviço: "));
                System.out.println("Serviço contratado.");
                break;
            }
            case "10": {
                String numero = ler("Nº da conta: ");
                String escolha = ler("Novo status (1-Ativa, 2-Bloqueada, 3-Encerrada): ");
                StatusConta status;
                switch (escolha) {
                    case "1": status = StatusConta.ATIVA; break;
                    case "2": status = StatusConta.BLOQUEADA; break;
                    case "3": status = StatusConta.ENCERRADA; break;
                    default: throw new ValorInvalidoException("Status inválido.");
                }
                banco.alterarStatus(numero, status);
                System.out.println("Status alterado para " + status + ".");
                break;
            }
            case "11": {
                for (String linha : banco.fecharMes()) {
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

    /** Extrato separado por mês, com o saldo acumulado de cada mês e o saldo atual no final. */
    private static void imprimirExtrato(Conta conta) {
        System.out.println("\n========== EXTRATO ==========");
        System.out.println(conta.getTipo() + " Nº " + conta.getNumero() + " | Titular: "
                + conta.getTitular().getNome() + " | " + conta.getStatus());
        System.out.println("Serviços: " + (conta.getServicos().isEmpty() ? "nenhum" : conta.getServicos()));

        Map<YearMonth, List<Movimentacao>> porMes = conta.getMovimentacoesPorMes();
        if (porMes.isEmpty()) {
            System.out.println("\nSem movimentações.");
        }

        // A conta nasce com saldo zero e todo crédito/débito gera uma movimentação,
        // então somar as movimentações em ordem reconstrói o saldo ao fim de cada mês.
        BigDecimal saldoAcumulado = BigDecimal.ZERO;
        for (Map.Entry<YearMonth, List<Movimentacao>> mes : porMes.entrySet()) {
            String nomeMes = mes.getKey().format(FORMATO_MES);
            System.out.println("\n--- " + nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1) + " ---");
            for (Movimentacao m : mes.getValue()) {
                System.out.println("  " + m);
                saldoAcumulado = saldoAcumulado.add(m.getValorComSinal());
            }
            System.out.println("  Saldo ao fim do mês: R$ " + saldoAcumulado);
        }

        System.out.println("\n=============================");
        System.out.println("SALDO ATUAL: R$ " + conta.getSaldo());
        if (conta.getSaldoDisponivel().compareTo(conta.getSaldo()) != 0) { // conta corrente: inclui o limite
            System.out.println("Disponível (com cheque especial): R$ " + conta.getSaldoDisponivel());
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
