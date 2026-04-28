using System;
using System.Threading.Tasks;
using System.Net.Http;

//https://github.com/xoofx/Tomlyn
using Tomlyn;
using Tomlyn.Model;

using lightning323.packInstaller.IO;

namespace lightning323.packInstaller
{

    class Program
    {
        private static readonly HttpClient client = new HttpClient();
        static Uri packTomlURL =  new Uri(
            "https://raw.githubusercontent.com/Lightning323/MC-Terranova/refs/heads/main/pack/pack.toml"
        );

        static async Task Main(string[] args)
        {
            string result = await client.GetStringAsync(packTomlURL);
            Console.WriteLine(result + "\n\n");
            var model = TomlSerializer.Deserialize<TomlTable>(result);

            if (model.ContainsKey("index"))
            {
                TomlTable index = (TomlTable) model["index"];
                string file = (string) index["file"];
                string hashFormat = (string) index["hash-format"];
                string hash = (string) index["hash"];

                Console.WriteLine(file);
                Console.WriteLine(hashFormat);
                Console.WriteLine(hash);
            
                Console.WriteLine( Reader.ReadFile(packTomlURL, file, hashFormat, hash));
            }
            // Console.WriteLine(index["file"].ToString());
        }
    }
}