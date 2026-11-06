# Auditoria — Cálculo Fácil Android

Data: 2026-11-06  
Base: `cálculo-fácil.zip` (web app React/Vite).

## O que o material original contém

- Web app funcional com cinco abas: básica, científica, conversores, financeiro e histórico.
- Parser matemático recursivo próprio (`mathParser.ts`), com funções trigonométricas, logaritmos, raízes, fatorial, porcentagem, constantes e multiplicação implícita.
- Conversores de unidades e moedas; moedas com tentativa de buscar taxas ao vivo em `open.er-api.com` e fallback offline.
- Cálculos financeiros: desconto, porcentagem, regra de 3, gorjeta, juros compostos e financiamento PRICE/SAC.
- Histórico, memória, ajustes de ângulo/formato/precisão/vibração/som e opções de tema.
- **Nenhum projeto Android**: o archive tem apenas o front-end web.
- `metadata.json` declara capacidade de Gemini no servidor, mas o app web não depende de IA para calcular.

## Decisões de implementação

| Tema | Web | Android nativo |
|---|---|---|
| UI | React/Tailwind | Kotlin + XML + Material 3 |
| Cálculo | parser TS | `MathEngine.kt`, parser recursivo portado |
| Estado | React hooks | `CalcSession` + `CalcStore` |
| Persistência | localStorage | SharedPreferences + JSON |
| Moedas | fetch ao vivo + fallback | taxas estáticas offline, com aviso |
| Histórico | localStorage | SharedPreferences, limite de 100 |
| Tema | auto/claro/escuro | escuro fixo |
| Som | Web Audio | vibração opcional |
| IA/Gemini | capacidade declarada | não integrada; app 100% offline |
| Android | inexistente | app completo, CI, APK assinado, testes |

## Funcionalidades verificadas

- Precedência de operadores, potência associativa à direita, unário, fatorial, porcentagem e multiplicação implícita.
- Funções trigonométricas em DEG/RAD, inversas, hiperbólicas, ln/log/log2, sqrt/cbrt/abs/exp.
- Formatação pt-BR e en-US, precisão automática ou fixa (2/4/6/8).
- Memória MC/MR/M+/M− e histórico com reutilização.
- Conversão por fatores e temperatura (C/F/K); moedas por taxa relativa ao USD.
- Fórmulas financeiras: desconto, porcentagem (3 modos), regra de 3 direta/inversa, gorjeta, juros compostos com aportes e PRICE/SAC.
- Teste de fluxo do diálogo financeiro salvando no histórico.

## O que foi deliberadamente omitido

- **Cotações ao vivo**: exigiria internet, permissão e uma API; a versão Android é offline e informa que as taxas são de referência.
- **Gemini/nuvem**: não é necessário para calcular e contradiz a proposta de privacidade offline.
- **Tema claro/auto e som**: não portados nesta 1.0.0; declarados no README.
- **Teclado físico**: foco em uso por toque no celular.
- Nenhum resultado ou histórico simulado: os valores são calculados localmente.

## Limitações

- Taxas de moedas podem ficar desatualizadas.
- Parsing de entrada usa convenção pt-BR quando há vírgula; entradas ambíguas com ponto podem ser interpretadas como milhar.
- Validação em Robolectric; recomenda-se teste em aparelho físico para teclado, haptics e layout em telas pequenas.
