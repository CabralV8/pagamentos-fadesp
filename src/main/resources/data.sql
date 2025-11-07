INSERT INTO pagamento (
  codigo_debito,
  cpf_cnpj_pagador,
  metodo_pagamento_enum,
  numero_cartao,
  valor_transacao,
  status,
  ativo
) VALUES
  (1001, '12345678901', 'PIX',               NULL, 250.00, 'PENDENTE',                 TRUE),
  (1002, '98765432100', 'PIX',               NULL, 125.75, 'PROCESSADO_COM_SUCESSO',   TRUE),
  (1003, '11122233344', 'CARTAO_CREDITO', '5555444433331111',  99.90, 'PENDENTE',     TRUE),
  (1004, '22233344455', 'BOLETO',            NULL,  45.00, 'PROCESSADO_COM_FALHA',     TRUE);
