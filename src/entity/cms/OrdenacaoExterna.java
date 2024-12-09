package entity.cms;
import java.util.*;
import java.io.*;

public class OrdenacaoExterna {

    // Método para dividir os dados em blocos menores e ordenar cada bloco
    public static void dividirEOordenarArquivo(String nomeArquivoEntrada, String nomeArquivoSaida, int tamanhoBloco) throws IOException, ClassNotFoundException {
        // Abrir o arquivo de entrada
        ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(nomeArquivoEntrada));

        List<List<Cliente>> blocos = new ArrayList<>();
        List<Cliente> blocoAtual = new ArrayList<>();

        // Ler clientes e dividir em blocos
        try {
            while (true) {
                try {
                    Cliente cliente = (Cliente) entrada.readObject();
                    blocoAtual.add(cliente);
                    if (blocoAtual.size() == tamanhoBloco) {
                        // Ordena o bloco e adiciona à lista de blocos
                        Collections.sort(blocoAtual);
                        blocos.add(new ArrayList<>(blocoAtual));
                        blocoAtual.clear();
                    }
                } catch (EOFException e) {
                    break;  // Fim do arquivo
                }
            }
        } finally {
            entrada.close();
        }

        // Ordena o último bloco, caso tenha sobrado elementos
        if (!blocoAtual.isEmpty()) {
            Collections.sort(blocoAtual);
            blocos.add(blocoAtual);
        }

        // Escrever os blocos ordenados em arquivos temporários
        List<String> arquivosTemporarios = new ArrayList<>();
        for (int i = 0; i < blocos.size(); i++) {
            String nomeTemp = "bloco_" + i + ".bin";
            arquivosTemporarios.add(nomeTemp);
            try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(nomeTemp))) {
                for (Cliente cliente : blocos.get(i)) {
                    saida.writeObject(cliente);
                }
            }
        }

        // Mesclar os blocos ordenados
        mesclarArquivos(arquivosTemporarios, nomeArquivoSaida);
    }

    // Método para mesclar os arquivos temporários em um arquivo final ordenado
    private static void mesclarArquivos(List<String> arquivosTemporarios, String nomeArquivoSaida) throws IOException, ClassNotFoundException {
        PriorityQueue<Cliente> pq = new PriorityQueue<>();
        List<ObjectInputStream> streams = new ArrayList<>();

        // Abrir os arquivos temporários
        for (String arquivo : arquivosTemporarios) {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(arquivo));
            streams.add(stream);
            try {
                // Adiciona o primeiro cliente de cada arquivo à fila de prioridade
                pq.offer((Cliente) stream.readObject());
            } catch (EOFException e) {
                // Nenhum cliente para adicionar
            }
        }

        // Criar o arquivo de saída
        ObjectOutputStream saída = new ObjectOutputStream(new FileOutputStream(nomeArquivoSaida));

        // Mesclar os arquivos temporários
        while (!pq.isEmpty()) {
            Cliente cliente = pq.poll();
            saída.writeObject(cliente);

            // Adiciona o próximo cliente do arquivo que forneceu o cliente extraído
            for (int i = 0; i < streams.size(); i++) {
                ObjectInputStream stream = streams.get(i);
                try {
                    Cliente proximoCliente = (Cliente) stream.readObject();
                    pq.offer(proximoCliente);
                    break;
                } catch (EOFException e) {
                    continue;
                }
            }
        }

        // Fechar o arquivo de saída
        saída.close();

        // Fechar os arquivos temporários
        for (ObjectInputStream stream : streams) {
            stream.close();
        }
    }

    public static void main(String[] args) {
        try {
            String nomeArquivoEntrada = "data";  // Arquivo com os 10 milhões de clientes
            String nomeArquivoSaida = "clientes_ordenados.bin";    // Arquivo de saída com os clientes ordenados
            int tamanhoBloco = 1000000;  // Tamanho do bloco para cada carga de dados em memória

            dividirEOordenarArquivo(nomeArquivoEntrada, nomeArquivoSaida, tamanhoBloco);

            System.out.println("Ordenação externa concluída com sucesso!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


