# Sistema Bancário — POO com Banco de Dados (N1, parte 2: Hibernate)

Sistema bancário em Java (console) com persistência via JPA/Hibernate e MySQL.

Requer JDK 25 (LTS) para compilar e executar. Para usar os comandos Maven abaixo, instale Maven 3.9 ou superior.

## Como executar

1. Deixe o MySQL rodando e confira usuário/senha em `src/main/resources/META-INF/persistence.xml`.
   O banco `banco_poo_jpa` e as tabelas são criados automaticamente na primeira execução.
2. No VS Code, abra a pasta e execute `banco.app.Main`, ou rode pelo terminal:
   `mvn compile exec:java`

## Critérios da atividade → onde estão

| Critério | Onde |
|---|---|
| @OneToMany / @ManyToOne bidirecional com `mappedBy` | `Cliente.contas` (mappedBy = "titular") ↔ `Conta.titular`; `Conta.movimentacoes` (mappedBy = "conta") ↔ `Movimentacao.conta` |
| @ManyToMany com @JoinTable | `Conta.servicos` → tabela `conta_servico` (sem classe Java) |
| @Embedded / @Embeddable | `Endereco` embutido em `Cliente` (colunas na tabela `cliente`) |
| @Enumerated(EnumType.STRING) | `Conta.status` (`StatusConta`) e `Movimentacao.tipo` (`TipoMovimentacao`) |
| Cascade | `Conta.movimentacoes` com `CascadeType.ALL`: salvar a conta grava as movimentações |

Os pilares da N1 continuam: abstração (`Conta`), herança (`ContaCorrente`, `ContaPoupanca`,
mapeada com `@Inheritance(SINGLE_TABLE)`), polimorfismo (`calcularTarifaMensal`, `getSaldoDisponivel`),
interface (`Rentavel` e os repositórios) e exceções (`banco.excecao`).

## Modelo

```
Cliente ──1:N── Conta ──1:N── Movimentacao
  └ Endereco      │  (Corrente / Poupança)
   (@Embedded)    └──N:M── Servico   (conta_servico)
```
