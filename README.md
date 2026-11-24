# Sistema Bancário — POO com Banco de Dados (N1, parte 2: Hibernate)

Sistema bancário em Java (console) com persistência via JPA/Hibernate e MySQL.

Requer JDK 25 (LTS) para compilar e executar. Para usar os comandos Maven abaixo, instale Maven 3.9 ou superior.
0

## Critérios da atividade → onde estão

| Critério | Onde |
|---|---|
| @OneToMany / @ManyToOne bidirecional com `mappedBy` | `Cliente.contas` (mappedBy = "titular") ↔ `Conta.titular`; `Conta.movimentacoes` (mappedBy = "conta") ↔ `Movimentacao.conta` |
| @ManyToMany com @JoinTable | `Conta.servicos` → tabela `conta_servico` (sem classe Java) |
| @Embedded / @Embeddable | `Endereco` embutido em `Cliente` (colunas na tabela `cliente`) |
| @Enumerated(EnumType.STRING) | `Conta.status` (`StatusConta`) e `Movimentacao.tipo` (`TipoMovimentacao`) |
| Cascade | `Conta.movimentacoes` com `CascadeType.ALL`: salvar a conta grava as movimentações |


## Modelo

```
Cliente ──1:N── Conta ──1:N── Movimentacao
  └ Endereco      │  (Corrente / Poupança)
   (@Embedded)    └──N:M── Servico   (conta_servico)
```
