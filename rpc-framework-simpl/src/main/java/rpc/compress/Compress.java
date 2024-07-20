package rpc.compress;

import rpc.extension.SPI;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 19:33
 */


@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
