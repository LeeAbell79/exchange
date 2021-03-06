package io.bisq.core.network;

import com.google.common.collect.Sets;
import com.google.inject.name.Named;
import io.bisq.core.app.BisqEnvironment;
import io.bisq.network.NetworkOptionKeys;
import io.bisq.network.p2p.NodeAddress;
import io.bisq.network.p2p.seed.SeedNodesRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CoreSeedNodesRepository implements SeedNodesRepository {

    @Getter
    private final Set<NodeAddress> seedNodeAddresses;
    @Getter
    private final Set<NodeAddress> seedNodeAddressesOldVersions;

    @Inject
    public CoreSeedNodesRepository(BisqEnvironment bisqEnvironment,
                                   @Named(NetworkOptionKeys.USE_LOCALHOST_FOR_P2P) boolean useLocalhostForP2P,
                                   @Named(NetworkOptionKeys.NETWORK_ID) int networkId,
                                   @Nullable @Named(NetworkOptionKeys.MY_ADDRESS) String myAddress,
                                   @Nullable @Named(NetworkOptionKeys.SEED_NODES_KEY) String seedNodes) {
        List<String> bannedNodes = bisqEnvironment.getBannedSeedNodes();
        Set<NodeAddress> nodeAddresses;
        Set<NodeAddress> nodeAddressesOldVersions;
        if (seedNodes != null && !seedNodes.isEmpty()) {
            nodeAddresses = Arrays.asList(StringUtils.deleteWhitespace(seedNodes).split(","))
                    .stream()
                    .map(NodeAddress::new)
                    .collect(Collectors.toSet());

            nodeAddressesOldVersions = new HashSet<>();
        } else {
            nodeAddresses = useLocalhostForP2P ? localhostSeedNodeAddresses : torSeedNodeAddresses;
            nodeAddresses = nodeAddresses.stream()
                    .filter(e -> String.valueOf(e.getPort()).endsWith("0" + String.valueOf(networkId)))
                    .collect(Collectors.toSet());

            if (!useLocalhostForP2P) {
                nodeAddressesOldVersions = torSeedNodeAddressesOldVersions.stream()
                        .filter(e -> String.valueOf(e.getPort()).endsWith("0" + String.valueOf(networkId)))
                        .collect(Collectors.toSet());
            } else {
                nodeAddressesOldVersions = new HashSet<>();
            }
        }

        seedNodeAddresses = nodeAddresses.stream()
                .filter(e -> myAddress == null || myAddress.isEmpty() || !e.getFullAddress().equals(myAddress))
                .filter(e -> bannedNodes == null || !bannedNodes.contains(e.getHostName()))
                .collect(Collectors.toSet());

        if (bannedNodes == null)
            log.info("seedNodeAddresses={}", seedNodeAddresses);
        else
            log.warn("We received banned seed nodes={}, seedNodeAddresses={}", bannedNodes, seedNodeAddresses);

        seedNodeAddressesOldVersions = nodeAddressesOldVersions.stream()
                .filter(e -> myAddress == null || myAddress.isEmpty() || !e.getFullAddress().equals(myAddress))
                .filter(e -> bannedNodes == null || !bannedNodes.contains(e.getHostName()))
                .collect(Collectors.toSet());
    }

    public String getOperator(NodeAddress nodeAddress) {
        switch (nodeAddress.getFullAddress()) {
            case "5quyxpxheyvzmb2d.onion:8000":
                return "@mrosseel";
            case "ef5qnzx6znifo3df.onion:8000":
                return "@ManfredKarrer";
            case "s67qglwhkgkyvr74.onion:8000":
                return "@emzy";
            case "jhgcy2won7xnslrb.onion:8000":
                return "@sqrrm";
            case "3f3cu2yw7u457ztq.onion:8000":
                return "@ManfredKarrer";
            case "723ljisnynbtdohi.onion:8000":
                return "@ManfredKarrer";
            case "rm7b56wbrcczpjvl.onion:8000":
                return "@ManfredKarrer";
            case "fl3mmribyxgrv63c.onion:8000":
                return "@ManfredKarrer";
            default:
                return "Undefined";
        }
    }

    /* old nodes pre 0.6.0 (still running for 0.5.* versions */
    private Set<NodeAddress> torSeedNodeAddressesOldVersions =
            Sets.newHashSet(
                    new NodeAddress("3f3cu2yw7u457ztq.onion:8000"), // @ManfredKarrer
                    new NodeAddress("723ljisnynbtdohi.onion:8000"), // @ManfredKarrer
                    new NodeAddress("rm7b56wbrcczpjvl.onion:8000"), // @ManfredKarrer
                    new NodeAddress("fl3mmribyxgrv63c.onion:8000")  // @ManfredKarrer
            );

    // Addresses are used if their port match the network id:
    // - mainnet uses port 8000
    // - testnet uses port 8001
    // - regtest uses port 8002
    @SuppressWarnings("ConstantConditions")
    private Set<NodeAddress> torSeedNodeAddresses = Sets.newHashSet(
            // BTC mainnet
            new NodeAddress("5quyxpxheyvzmb2d.onion:8000"), // @mrosseel
            new NodeAddress("ef5qnzx6znifo3df.onion:8000"), // @ManfredKarrer
            new NodeAddress("s67qglwhkgkyvr74.onion:8000"), // @emzy
            new NodeAddress("jhgcy2won7xnslrb.onion:8000"), // @sqrrm

            //TODO dev
            // local dev
            // new NodeAddress("joehwtpe7ijnz4df.onion:8000"),
            // new NodeAddress("uqxi3zrpobhtoes6.onion:8000"),

            // BTC testnet
            new NodeAddress("nbphlanpgbei4okt.onion:8001"),
            // new NodeAddress("vjkh4ykq7x5skdlt.onion:8001"), // dev test

            // BTC regtest
            // For development you need to change that to your local onion addresses
            // 1. Run a seed node with prog args: --bitcoinNetwork=regtest --nodePort=8002 --myAddress=rxdkppp3vicnbgqt:8002 --appName=bisq_seed_node_rxdkppp3vicnbgqt.onion_8002
            // 2. Find your local onion address in bisq_seed_node_rxdkppp3vicnbgqt.onion_8002/regtest/tor/hiddenservice/hostname
            // 3. Shut down the seed node
            // 4. Rename the directory with your local onion address
            // 5. Edit here your found onion address (new NodeAddress("YOUR_ONION.onion:8002")
            new NodeAddress("rxdkppp3vicnbgqt.onion:8002"),

            // LTC mainnet
            new NodeAddress("acyvotgewx46pebw.onion:8003"),
            new NodeAddress("pklgy3vdfn3obkur.onion:8003"),

            // DOGE mainnet
            // new NodeAddress("t6bwuj75mvxswavs.onion:8006"), removed in version 0.6 (DOGE not supported anymore)

            // DASH mainnet
            new NodeAddress("toeu5ikb27ydscxt.onion:8009"),
            new NodeAddress("ae4yvaivhnekkhqf.onion:8009")
    );

    // Addresses are used if the last digit of their port match the network id:
    // - mainnet use port ends in 0
    // - testnet use port ends in 1
    // - regtest use port ends in 2
    private Set<NodeAddress> localhostSeedNodeAddresses = Sets.newHashSet(
            // BTC
            // mainnet
            new NodeAddress("localhost:2000"),
            new NodeAddress("localhost:3000"),
            new NodeAddress("localhost:4000"),

            // testnet
            new NodeAddress("localhost:2001"),
            new NodeAddress("localhost:3001"),
            new NodeAddress("localhost:4001"),

            // regtest
            new NodeAddress("localhost:2002"),
            new NodeAddress("localhost:3002"),
         /*    new NodeAddress("localhost:4002"),*/

            // LTC
            // mainnet
            new NodeAddress("localhost:2003"),

            // regtest
            new NodeAddress("localhost:2005"),

            // DOGE regtest
            new NodeAddress("localhost:2008"),

            // DASH regtest
            new NodeAddress("localhost:2011")
    );


    public void setTorSeedNodeAddresses(Set<NodeAddress> torSeedNodeAddresses) {
        this.torSeedNodeAddresses = torSeedNodeAddresses;
    }

    public void setLocalhostSeedNodeAddresses(Set<NodeAddress> localhostSeedNodeAddresses) {
        this.localhostSeedNodeAddresses = localhostSeedNodeAddresses;
    }

    public boolean isSeedNode(NodeAddress nodeAddress) {
        return Stream.concat(localhostSeedNodeAddresses.stream(), torSeedNodeAddresses.stream())
                .filter(e -> e.equals(nodeAddress)).findAny().isPresent();
    }
}
